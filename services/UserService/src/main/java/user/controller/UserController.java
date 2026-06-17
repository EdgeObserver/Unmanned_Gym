package user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pojo.ResultMsg;
import pojo.User;
import pojo.UserPageParam;
import user.dto.UserDto;
import user.service.UserService;

@RestController
//@RequestMapping("/api/user")
//@CrossOrigin  // 处理跨域
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResultMsg login(@RequestParam String userId, @RequestParam String password) {
        System.out.println("id:"+userId+"用户登录");
        return userService.login(userId, password);
    }
    @PostMapping("/managerLogin")
    public ResultMsg managerLogin(@RequestParam String userId, @RequestParam String password) {
        return userService.managerLogin(userId, password);
    }

    /**
     * 用户注册
     */

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResultMsg register(UserDto userDto,
                              @RequestPart("pic") MultipartFile avatarFile) {
        return userService.register(userDto, avatarFile);
    }

    /**
     * 获取所有用户（非删除状态）
     */
    @GetMapping("/all")
    public ResultMsg findAll() {
        return userService.findAll();
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    public ResultMsg findAllPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return userService.findAllPage(pageNum, pageSize);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public ResultMsg update(@RequestBody User user) {
        return userService.update(user);
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public ResultMsg getById(@RequestParam Integer id) {
        return userService.getById(id);
    }
    @GetMapping("/checkOrder")
    public ResultMsg checkOrder(@RequestParam Integer id) {
        return userService.checkOrder(id);
    }
    @DeleteMapping("/delete")
    public ResultMsg deleteById(@RequestParam Integer id) {
        return userService.deleteById(id);
    }

    /**
     * 批量删除用户（软删除）
     */
    @DeleteMapping("/batch-delete")
    public ResultMsg deleteByBatchIds(@RequestParam int[] ids) {
        return userService.deleteByBatchIds(ids);
    }

    /**
     * 分页条件查询用户列表
     */
    @PostMapping("/list")
    public ResultMsg getList(@RequestBody UserPageParam userPageParam) {
        return userService.getList(userPageParam);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ResultMsg info() {
        return userService.info();
    }

    /**
     * 根据userId获取用户ID
     */
    @GetMapping("/id/{userId}")
    public ResultMsg getIdByUserId(@PathVariable String userId) {
        return userService.getIdByUserId(userId);
    }

    /**
     * 根据userId获取用户信息
     */
    @GetMapping("/by-userId/{userId}")
    public ResultMsg getByUserId(@PathVariable String username) {
        return userService.getByUsername(username);
    }
}
