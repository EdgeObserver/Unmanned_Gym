package com.AttendanceSystem.controller;

import com.AttendanceSystem.dto.UserDto;
import com.AttendanceSystem.pojo.ResultMsg;
import com.AttendanceSystem.pojo.User;
import com.AttendanceSystem.pojo.UserPageParam;
import com.AttendanceSystem.service.user.Impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/user")
@EnableCaching
public class UserController {

    @Autowired
    private UserServiceImpl UserService;

    @PostMapping("register")
    public ResultMsg register(UserDto userDto,@RequestParam("pic") MultipartFile pic){
        return UserService.register(userDto,pic);
    }

    @RequestMapping("findAll")
    public ResultMsg findAll(){
        return UserService.findAll();
    }

    @RequestMapping("findAllPage")
    public ResultMsg findAllPage(Integer pageNum, Integer pageSize){
        return UserService.findAllPage(pageNum,pageSize);
    }

    @RequestMapping("update")
//    @CachePut(value = "user",key = "#sysUser.id")
    public ResultMsg update(@RequestBody User sysUser){
        return UserService.update(sysUser);
    }

    @RequestMapping("getById")
//    @Cacheable(value = "user",key = "#id")
    public ResultMsg getById(String id){
        return UserService.getById(id);
    }

    @DeleteMapping("deleteByBatch/{ids}")
    public ResultMsg deleteByBatch(@PathVariable String[] ids){
        return UserService.deleteByBatchIds(ids);
    }

    //Service类中的查找方法在这里要用于构造搜索接口
    @RequestMapping("search")
//    @Cacheable(value = "userCache", key = "#sysUserPageParam")
    public ResultMsg search(@RequestBody UserPageParam sysUserPageParam){
        System.out.println("search not in cache");
        return UserService.getList(sysUserPageParam);
    }


}