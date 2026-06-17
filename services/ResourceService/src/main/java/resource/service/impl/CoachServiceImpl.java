package resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.Coach;
import pojo.ResultMsg;
import pojo.TimeSlot;
import resource.dto.CoachDto;
import resource.mapper.CoachMapper;
import resource.mapper.TimeSlotMapper;
import resource.service.CoachService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CoachServiceImpl implements CoachService {

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    @Override
    public ResultMsg findAllCoach() {
        QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        List<Coach> coaches = coachMapper.selectList(queryWrapper);
        return ResultMsg.success(coaches, "查找成功");
    }

    @Override
    public ResultMsg findAllCoachPage(Integer pageNum, Integer pageSize) {
        Page<Coach> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        page = coachMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(page, "操作成功");
    }

    @Override
    public ResultMsg getCoachById(int id) {
        QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("id", id);
        Coach coach = coachMapper.selectOne(queryWrapper);

        if (coach != null) {
            return ResultMsg.success(coach, "操作成功");
        }
        return ResultMsg.fail(null, "教练不存在");
    }

    @Override
    public ResultMsg addCoach(CoachDto coachDto) {
        String name = coachDto.getName();
        if(getByName(name).getData() != null){
            return ResultMsg.fail(null, "教练已存在");
        }

        Coach coach = new Coach();
        coach.setName(coachDto.getName());
        coach.setIsDeleted("n");
        coach.setStatus(1);
        coach.setCreatedTime(LocalDateTime.now());
        coach.setUpdatedTime(LocalDateTime.now());
        coachMapper.insert(coach);

        return ResultMsg.success("", "添加成功");
    }

    @Override
    public ResultMsg updateCoach(Coach coach) {
        coach.setUpdatedTime(LocalDateTime.now());
        coachMapper.updateById(coach);
        return ResultMsg.success(null, "更新成功");
    }

    @Override
    public ResultMsg deleteCoach(int id) {
        QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("id", id);
        Coach coach = coachMapper.selectOne(queryWrapper);

        if (coach == null) {
            return ResultMsg.fail(null, "教练不存在");
        }

        coach.setIsDeleted("y");
        coach.setUpdatedTime(LocalDateTime.now());
        coachMapper.updateById(coach);

        return ResultMsg.success(null, "删除成功");
    }

    @Override
    public ResultMsg getByName(String name) {
        QueryWrapper<Coach> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("name", name);
        Coach coach = coachMapper.selectOne(queryWrapper);
        return ResultMsg.success(coach, "查询成功");
    }

    public ResultMsg findAllTimeSlots() {
        QueryWrapper<TimeSlot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.orderByAsc("start_time");
        List<TimeSlot> timeSlots = timeSlotMapper.selectList(queryWrapper);
        return ResultMsg.success(timeSlots, "查询成功");
    }
}
