package com.AttendanceSystem.service.action_record.impl;

import com.AttendanceSystem.mapper.ActionRecordMapper;
import com.AttendanceSystem.pojo.ActionRecord;
import com.AttendanceSystem.pojo.ResultMsg;
import com.AttendanceSystem.pojo.User;
import com.AttendanceSystem.pojo.UserPageParam;
import com.AttendanceSystem.service.action_record.ActionRecordService;
import com.AttendanceSystem.util.ThreadLocalUtil;
import com.AttendanceSystem.util.TypeConvertUtil;
import com.auth0.jwt.interfaces.Claim;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ActionRecordServiceImpl implements ActionRecordService {

    @Autowired
    private ActionRecordMapper actionRecordMapper;

    @Override
    public ResultMsg arrive(Integer uid) {
        ActionRecord actionRecord=new ActionRecord();
        actionRecord.setUid(uid);
        actionRecord.setArrive_time(LocalDateTime.now());
        actionRecord.setStatus(true);
        actionRecordMapper.insert(actionRecord);
        return ResultMsg.success("","上传活动记录成功");
    }

    @Override
    public ResultMsg leave(Integer uid) {
        ActionRecord actionRecord = actionRecordMapper.selectOne(
                new QueryWrapper<ActionRecord>()
                        .eq("uid", uid)
                        .eq("status", 1));

        actionRecord.setLeave_time(LocalDateTime.now());
        actionRecord.setStatus(false);
        actionRecordMapper.updateById(actionRecord);
        return ResultMsg.success("","更新活动记录成功");
    }

    @Override
    public ResultMsg delete(Integer id) {
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        ActionRecord actionRecord=actionRecordMapper.selectOne(queryWrapper);
        actionRecord.setIs_deleted("y");
        actionRecordMapper.updateById(actionRecord);
        return ResultMsg.success("","删除成功");
    }

    @Override
    public ResultMsg findAll() {
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        List<ActionRecord> records = actionRecordMapper.selectList(queryWrapper);
        return ResultMsg.success(records, "查找成功");
    }

    @Override
    public ResultMsg findAllPage(Integer pageNum, Integer pageSize) {
        Page<ActionRecord> page = new Page<>(pageNum, pageSize);
        //把所有isDelete为“n”的数据筛选出来并且分页
        QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        page = actionRecordMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(page, "操作成功");
    }



    @Override
    public ResultMsg deleteByBatchIds(String[] ids) {
        Map<String, Claim> map= ThreadLocalUtil.get();
        String currUserId = map.get("actionRecordId").asString();
        List<String> list = TypeConvertUtil.convertIdsToList(ids);

        for (String id : list){
            QueryWrapper<ActionRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_deleted", "n");
            queryWrapper.eq("id", id);
            ActionRecord actionRecord =actionRecordMapper.selectOne(queryWrapper);

            actionRecord.setIsDeleted("y");

            actionRecordMapper.updateById(actionRecord);
        }

        return ResultMsg.success(null, "删除成功");
    }

    @Override
    public ResultMsg getList(UserPageParam sysUserPageParam) {
        return null;
    }

    @Override
    public ResultMsg info() {
        return null;
    }

    @Override
    public ResultMsg getIdByUserId(String userId) {
        return null;
    }

    @Override
    public ResultMsg getByUserId(String userId) {
        return null;
    }
}
