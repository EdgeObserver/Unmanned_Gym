package user.service.Impl;

import com.auth0.jwt.interfaces.Claim;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import pojo.*;

import user.dto.UserDto;
import user.feign.OrderClient;
import user.mapper.ManagerMapper;
import user.mapper.UserMapper;
import user.service.UserService;
import user.utils.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.temporal.ChronoUnit;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ManagerMapper managerMapper;

    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    private OssAvatarUtil ossAvatarUtil;

    @Autowired
    private OrderClient orderClient;


    @Transactional
    @Override
    public ResultMsg login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("is_deleted", "n");

        User user = userMapper.selectOne(queryWrapper);


        // 2. 判断用户是否存在
        if (user == null) {
            return ResultMsg.fail(null, "用户不存在或密码错误");
        }
        if (BCryptUtil.checkpw(password, user.getPassword())) {
            // 3. 检查会员状态（直接使用用户表中的 membershipEndTime 字段）
            LocalDate membershipEndTime = user.getMembershipEndTime();
            
            // 如果会员到期时间为空或已过期
            if (membershipEndTime == null || membershipEndTime.isBefore(LocalDate.now())) {
                // 生成token但标记为需要续费
                Map<String, Integer> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("role", 0);
                String token = JWTUtil.getToken(map);
                redisService.setToken(user.getId(), token, 60 * 1440);
                
                // 返回特殊消息，前端检测到后跳转到续费页面
                return ResultMsg.success(token, "登录成功，但会员已过期，请续费");
            }
            
            // 会员有效，正常登录
            Map<String, Integer> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("role", 0); // 0 代表普通用户
            String token = JWTUtil.getToken(map);

            redisService.setToken(user.getId(), token, 60 * 1440);

            return ResultMsg.success(token, "登录成功");
        }
        System.out.println(BCryptUtil.getpw(password));
        return ResultMsg.fail(null, "用户不存在或密码错误");
    }

    @Override
    public ResultMsg managerLogin(String username, String password) {
        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("is_deleted", "n");

        Manager manager = managerMapper.selectOne(queryWrapper);


        // 2. 判断用户是否存在
        if (manager == null) {
            return ResultMsg.fail(null, "管理员不存在或密码错误");
        }
        if (BCryptUtil.checkpw(password, manager.getPassword())) {
            //生成token并返回
            Map<String, Integer> map = new HashMap<>();
            map.put("id", manager.getId());
            map.put("role", 1  ); // 0 代表普通用户
            String token = JWTUtil.getToken(map);

            redisService.setToken(manager.getId(), token, 60 * 1440);

            return ResultMsg.success(token, "登录成功");
        }
        return ResultMsg.fail(null, "管理员不存在或密码错误");
    }
    public ResultMsg register(UserDto dto, MultipartFile avatarFile) {
        String username = dto.getUsername();
        System.out.println(dto.getUsername());
        // 检查用户名是否已存在，如果存在则不能注册
        if(getByUsername(username).getData() != null){
            return ResultMsg.fail(null, "用户已存在");
        }
        
        // 验证套餐ID
        if (dto.getPackageId() == null) {
            return ResultMsg.fail(null, "请选择套餐");
        }
        
        // 获取套餐信息
        pojo.Package pkg = orderClient.getPackageById(dto.getPackageId()).getData();
        if (pkg == null) {
            return ResultMsg.fail(null, "套餐不存在");
        }
        
        // ① 先插入，拿到自增主键（MyBatis-Plus 默认返回实体里已赋值）
        User user = new User();

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(BCryptUtil.getpw(dto.getPassword()));
        user.setCreatedTime(LocalDateTime.now());
        
        // 设置会员结束时间：当前日期 + 套餐天数
        user.setMembershipEndTime(LocalDate.now().plusDays(pkg.getDuration()));
        
        userMapper.insert(user);          // id 已被填充
        System.out.println(user.getId());
        // ② 用 id 当文件名上传头像
        String avatarUrl = ossAvatarUtil.uploadAvatar(avatarFile, user.getId()).getData();
        System.out.println(avatarUrl);
        // ③ 回写头像地址
        user.setUserPic(avatarUrl);
        userMapper.updateById(user);

        return ResultMsg.success("","注册成功");
    }
    @Override
    public ResultMsg findAll() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        List<User> sysUsers = userMapper.selectList(queryWrapper);
        return ResultMsg.success(sysUsers, "查找成功");
    }

    @Override
    public ResultMsg findAllPage(Integer pageNum, Integer pageSize) {
        //用分页来返回数据
        Page<User> page = new Page<>(pageNum, pageSize);
        //把所有isDelete为“n”的数据筛选出来并且分页
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        page = userMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(page, "操作成功");
    }


    @Override
    public ResultMsg update(User sysUser) {
        // 如果提供了新密码，需要进行BCrypt加密
        if (sysUser.getPassword() != null && !sysUser.getPassword().isEmpty()) {
            // 检查是否已经是BCrypt格式（以$2开头）
            if (!sysUser.getPassword().startsWith("$2")) {
                // 明文密码，需要加密
                sysUser.setPassword(BCryptUtil.getpw(sysUser.getPassword()));
            }
            // 如果已经是BCrypt格式，则直接使用
        } else {
            // 没有提供密码，从数据库查询原有密码
            User existingUser = userMapper.selectById(sysUser.getId());
            if (existingUser != null) {
                sysUser.setPassword(existingUser.getPassword());
            }
        }
        
        sysUser.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(sysUser);
        return ResultMsg.success(null, "更新成功");
    }

    @Override
    public ResultMsg getById(int id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("id", id);
//        System.out.println(id);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null) {
            return ResultMsg.success(user, "操作成功");
        }
        return ResultMsg.fail(null, "用户不存在");
    }
    @Override
    public ResultMsg deleteById(int id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("id", id);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            return ResultMsg.fail(null, "用户不存在");
        }

        try {
            ResultMsg<String> res = ossAvatarUtil.moveFileToIllegal(user.getUserPic());
            if (res.getCode() == 200) {
                user.setUserPic(res.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        user.setIsDeleted("y");
        user.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(user);

        return ResultMsg.success(null, "删除成功");
    }
    @Override
    public ResultMsg deleteByBatchIds(int[] ids) {
        for (int id : ids){
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_deleted", "n");
            queryWrapper.eq("id", id);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                return ResultMsg.fail(null, "用户不存在");
            }

            try {
                ResultMsg<String> res = ossAvatarUtil.moveFileToIllegal(user.getUserPic());
                if (res.getCode() == 200) {
                    user.setUserPic(res.getData());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            user.setIsDeleted("y");
            user.setUpdatedTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        return ResultMsg.success(null, "删除成功");
    }


    /*
    const pageParam=reactive({
	pageNum:1,//当前页
	pageSize:2,//每页条数
	userName:'',
	realName:'',
	roleId:''
})

     */
    @Override
    public ResultMsg getList(UserPageParam sysUserPageParam) {
        IPage<User> page = new Page<>(sysUserPageParam.getPageNum(), sysUserPageParam.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.like("user_id", sysUserPageParam.getUserId());
        queryWrapper.like("name", sysUserPageParam.getName());
        //如果未指定roleid，则默认查询所有角色
        if (sysUserPageParam.getRoleId() != null) {
            queryWrapper.eq("role_id", sysUserPageParam.getRoleId());
        }
        userMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(page, "操作成功");
    }

    @Override
    public ResultMsg info() {
        return null;
    }

    @Override
    public ResultMsg getIdByUserId(String userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("user_id", userId);
        User user = userMapper.selectOne(queryWrapper);

        return ResultMsg.success(user.getId(), "查询成功");
    }

    @Override
    public ResultMsg getByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        return ResultMsg.success(user,"查询成功");
    }

    @Override
    public ResultMsg checkOrder(Integer uid) {

        ResultMsg<Order> res = orderClient.getLegalOrderbyUid(uid);
        if (res.getData() == null) {
            return ResultMsg.fail(null, "您的订单已过期");
        }

        Order order = res.getData();
        if (order.getOrderEndTime().isBefore(LocalDate.now())) {
            // 订单结束时间早于当前时间，将订单状态设为0（失效）
            order.setStatus(0);
            orderClient.update(order); // 假设提供了更新订单状态的方法
            return ResultMsg.fail(null, "您的订单已过期");
        }


        int remainingDays = (int)ChronoUnit.DAYS.between(LocalDate.now(), order.getOrderEndTime()) + 1;
        return ResultMsg.success(remainingDays, "订单有效");

    }




}
