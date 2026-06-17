package com.AttendanceSystem.service.action_record;

import com.AttendanceSystem.pojo.ResultMsg;
import com.AttendanceSystem.pojo.User;
import com.AttendanceSystem.pojo.UserPageParam;

public interface ActionRecordService {
    public ResultMsg arrive(Integer id);
    public ResultMsg leave(Integer id);
    public ResultMsg delete(Integer id);
    public ResultMsg findAll();

    public ResultMsg findAllPage(Integer pageNum, Integer pageSize);

    public ResultMsg update(User sysUser);

    public ResultMsg getById(String userId);

    public ResultMsg deleteByBatchIds(String[] ids);

    public ResultMsg getList(UserPageParam sysUserPageParam);

    public ResultMsg info();

    public ResultMsg getIdByUserId(String userId);

    public ResultMsg getByUserId(String userId);
}
