package record.service.impl;

import record.mapper.ActionRecordMapper;
import record.service.ActionRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.ActionRecord;
import pojo.RecordPageParam;
import pojo.ResultMsg;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActionRecordServiceImpl implements ActionRecordService {
    
    @Autowired
    private ActionRecordMapper actionRecordMapper;


    @Override
    public ResultMsg arrive(int userId) {
        ActionRecord record = new ActionRecord();
        record.setUserId(userId);
        record.setArrivalTime(LocalDateTime.now());

        actionRecordMapper.insert(record);
        
        ResultMsg resultMsg = ResultMsg.success(null,"添加活动记录成功");
        return resultMsg;
    }

    @Override
    public ResultMsg deleteActionRecordById(int id) {
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "N");
        queryWrapper.eq("id", id);
        ActionRecord record = actionRecordMapper.selectOne(queryWrapper);

        if ( record == null) {
            return ResultMsg.fail(null, "记录不存在");
        }


        record.setIsDeleted("y");
        record.setUpdatedTime(LocalDateTime.now());
        actionRecordMapper.updateById(record);

        return ResultMsg.success(null, "删除成功");
    }

    @Override
    public ResultMsg leave(int userId) {
        System.out.println("========== 开始处理签退 ==========");
        System.out.println("用户ID: " + userId);
        
        // 1. 查询该用户所有未离开的记录（departureTime为null且未删除）
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_deleted", "N");
        queryWrapper.isNull("departure_time");
        queryWrapper.orderByDesc("arrival_time"); // 按到达时间降序排列
        
        List<ActionRecord> unDepartedRecords = actionRecordMapper.selectList(queryWrapper);
        
        System.out.println("找到未离开记录数: " + unDepartedRecords.size());
        
        if (unDepartedRecords == null || unDepartedRecords.isEmpty()) {
            System.out.println("警告: 没有找到该用户的未离开记录");
            return ResultMsg.fail(null, "没有找到该用户的未离开记录");
        }
        
        // 2. 第一条记录是到达时间最近的，填充离开时间
        ActionRecord latestRecord = unDepartedRecords.get(0);
        latestRecord.setDepartureTime(LocalDateTime.now());
        latestRecord.setUpdatedTime(LocalDateTime.now());
        actionRecordMapper.updateById(latestRecord);
        
        System.out.println("已填充最近记录的离开时间，记录ID: " + latestRecord.getId());
        System.out.println("到达时间: " + latestRecord.getArrivalTime());
        System.out.println("离开时间: " + latestRecord.getDepartureTime());
        
        // 3. 将其余记录软删除
        int deletedCount = 0;
        for (int i = 1; i < unDepartedRecords.size(); i++) {
            ActionRecord recordToDelete = unDepartedRecords.get(i);
            recordToDelete.setIsDeleted("Y");
            recordToDelete.setUpdatedTime(LocalDateTime.now());
            actionRecordMapper.updateById(recordToDelete);
            deletedCount++;
            
            System.out.println("已软删除重复记录，记录ID: " + recordToDelete.getId());
            System.out.println("到达时间: " + recordToDelete.getArrivalTime());
        }
        
        System.out.println("共软删除 " + deletedCount + " 条重复记录");
        System.out.println("==================================");
        
        return ResultMsg.success(null, "用户离开成功，处理了" + unDepartedRecords.size() + "条记录，软删除" + deletedCount + "条重复记录");
    }

    @Override
    public ResultMsg getActionRecordById(int id) {
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "N");
        queryWrapper.eq("id", id);
        ActionRecord record = actionRecordMapper.selectOne(queryWrapper);

        if (record == null) {
            return ResultMsg.fail(null,"记录不存在");
        }
        
        ResultMsg resultMsg = ResultMsg.success(record,"获取记录成功");

        return resultMsg;
    }

    @Override
    public ResultMsg findAllPage(int pageNum, int pageSize) {
        Page<ActionRecord> page = new Page<>(pageNum, pageSize);
        //把所有isDelete为“n”的数据筛选出来并且分页
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "N");
        page = actionRecordMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(page, "操作成功");
    }

    @Override
    public ResultMsg getList(RecordPageParam recordPageParam) {
        IPage<ActionRecord> page = new Page<>(recordPageParam.getPageNum(), recordPageParam.getPageSize());
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("is_deleted", "N");
        queryWrapper.eq("user_id", recordPageParam.getUserId());
        
        // 添加时间段查询条件
        if (recordPageParam.getStartTime() != null || recordPageParam.getEndTime() != null) {
            if (recordPageParam.getStartTime() != null && recordPageParam.getEndTime() != null) {
                queryWrapper.between("arrival_time", recordPageParam.getStartTime(), recordPageParam.getEndTime());
            } else if (recordPageParam.getStartTime() != null) {
                queryWrapper.ge("arrival_time", recordPageParam.getStartTime());
            } else {
                queryWrapper.le("arrival_time", recordPageParam.getEndTime());
            }
        }

        //如果未指定roleid，则默认查询所有角色

        actionRecordMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(page, "操作成功");
    }

    @Override
    public ResultMsg getTodayVisitCount() {
        // 获取今天的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        System.out.println("========== 今日访问人数统计 ==========");
        System.out.println("当前日期: " + today);
        System.out.println("开始时间: " + startOfDay);
        System.out.println("结束时间: " + endOfDay);
        
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "N");
        queryWrapper.ge("arrival_time", startOfDay);
        queryWrapper.lt("arrival_time", endOfDay);
        
        Long count = actionRecordMapper.selectCount(queryWrapper);
        System.out.println("查询结果: " + count);
        System.out.println("====================================");
        
        return ResultMsg.success(count.intValue(), "获取今日访问人数成功");
    }

    @Override
    public ResultMsg getWeeklyVisitStatistics() {
        List<Map<String, Object>> weeklyStats = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // 获取近7天的数据
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_deleted", "N");  // 修改为大写 N
            queryWrapper.ge("arrival_time", startOfDay);
            queryWrapper.lt("arrival_time", endOfDay);
            
            Long count = actionRecordMapper.selectCount(queryWrapper);
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.toString());
            dayStat.put("count", count.intValue());
            weeklyStats.add(dayStat);
        }
        
        return ResultMsg.success(weeklyStats, "获取近7天访问统计成功");
    }
}
