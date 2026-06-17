package com.AttendanceSystem.controller;

import com.AttendanceSystem.pojo.ResultMsg;
import com.AttendanceSystem.pojo.User;
import com.AttendanceSystem.pojo.UserPageParam;
import com.AttendanceSystem.service.action_record.impl.ActionRecordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/action_record")
public class ActionRecordController {
    @Autowired
    private ActionRecordServiceImpl actionRecordService;

    @RequestMapping("findAll")
    public ResultMsg findAll(){
        return actionRecordService.findAll();
    }

    @RequestMapping("findAllPage")
    public ResultMsg findAllPage(Integer pageNum, Integer pageSize){
        return actionRecordService.findAllPage(pageNum,pageSize);
    }
    public ResultMsg delete(Integer id){
        return actionRecordService.delete(id);
    }
    @RequestMapping("update")
//    @CachePut(value = "user",key = "#sysUser.id")
    public ResultMsg update(@RequestBody User sysUser){
        return actionRecordService.update(sysUser);
    }

    @RequestMapping("getById")
//    @Cacheable(value = "user",key = "#id")
    public ResultMsg getById(String id){
        return actionRecordService.getById(id);
    }

    @DeleteMapping("deleteByBatch/{ids}")
    public ResultMsg deleteByBatch(@PathVariable String[] ids){
        return actionRecordService.deleteByBatchIds(ids);
    }

    //Service类中的查找方法在这里要用于构造搜索接口
    @RequestMapping("search")
//    @Cacheable(value = "userCache", key = "#sysUserPageParam")
    public ResultMsg search(@RequestBody UserPageParam sysUserPageParam){
        System.out.println("search not in cache");
        return actionRecordService.getList(sysUserPageParam);
    }

    @GetMapping("/info")
    public ResultMsg info(){
        return actionRecordService.info();
    }


    @PostMapping("getIdByUserId")
    public ResultMsg getIdByUserId(String userId){
        return actionRecordService.getIdByUserId(userId);
    }

    @PostMapping("getByUserId")
    public ResultMsg getByUserId(String userId){
        return actionRecordService.getByUserId(userId);
    }



}
