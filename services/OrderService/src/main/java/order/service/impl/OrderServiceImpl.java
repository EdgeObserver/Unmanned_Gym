package order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import order.dto.OrderCreateDTO;
import order.mapper.OrderMapper;
import order.mapper.PackageMapper;
import order.service.OrderService;
import order.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.Order;
import pojo.Package;
import pojo.ResultMsg;
import pojo.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PackageMapper packageMapper;

    @Autowired
    private order.mapper.UserMapper userMapper;

    private Integer getCurrentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) {
            return null;
        }
        return (Integer) claims.get("id");
    }

    private Integer getCurrentUserRole() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) {
            return null;
        }
        return (Integer) claims.get("role");
    }

    private ResultMsg checkUserAuth() {
        Integer uid = getCurrentUserId();
        if (uid == null) {
            return ResultMsg.fail(null, "未获取到用户信息，请通过网关访问");
        }
        return null;
    }

    private void checkAdminPermission() {
        Integer role = getCurrentUserRole();
        if (role == null || role != 1) {
            throw new RuntimeException("权限不足，仅管理员可操作");
        }
    }

    @Override
    public ResultMsg<Order> getLegalOrderByUid(int uid) {
        // 获取用户信息
        User user = userMapper.selectById(uid);
        if (user == null) {
            return ResultMsg.fail(null, "用户不存在");
        }

        // 检查用户是否有有效的会员资格
        LocalDate now = LocalDate.now();
        if (user.getMembershipEndTime() == null || user.getMembershipEndTime().isBefore(now)) {
            return ResultMsg.fail(null, "未找到有效订单");
        }

        // 获取用户最新的订单记录
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.ge("order_end_time", now);  // 临时方案：直接查询有效订单
        queryWrapper.orderByDesc("order_start_time");
        queryWrapper.last("LIMIT 1");

        Order order = orderMapper.selectOne(queryWrapper);

        if (order == null) {
            return ResultMsg.fail(null, "未找到有效订单");
        }

        return ResultMsg.success(order, "查询成功");
    }


    @Override
    public ResultMsg<Order> getOrderById(int id) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("is_deleted", "n");

        Order order = orderMapper.selectOne(queryWrapper);

        if (order == null) {
            return ResultMsg.fail(null, "订单不存在");
        }

        return ResultMsg.success(order, "查询成功");
    }


    @Override
    public ResultMsg<Order> createOrder(OrderCreateDTO dto) {
        ResultMsg authCheck = checkUserAuth();
        if (authCheck != null) {
            return authCheck;
        }

        if (dto.getPid() == null || dto.getPid() == 0) {
            return ResultMsg.fail(null, "套餐ID不能为空");
        }

        Integer uid = getCurrentUserId();

        Package packageInfo = packageMapper.selectById(dto.getPid());
        if (packageInfo == null) {
            return ResultMsg.fail(null, "套餐不存在");
        }

        // 获取用户信息
        User user = userMapper.selectById(uid);
        if (user == null) {
            return ResultMsg.fail(null, "用户不存在");
        }

        // 计算新的订单结束时间
        LocalDate now = LocalDate.now();
        LocalDate orderStartTime = now;
        LocalDate orderEndTime;
        
        // TODO: 待数据库迁移后启用以下逻辑
        // 如果用户已有会员有效期，且未过期，则从当前结束时间延续
        // if (user.getMembershipEndTime() != null && user.getMembershipEndTime().isAfter(now)) {
        //     orderEndTime = user.getMembershipEndTime().plusDays(packageInfo.getDuration());
        // } else {
        //     // 否则从现在开始计算
        //     orderEndTime = now.plusDays(packageInfo.getDuration());
        // }
        
        // 临时方案：直接从现在开始计算
        orderEndTime = now.plusDays(packageInfo.getDuration());

        // 创建新订单记录
        Order newOrder = new Order();
        newOrder.setUid(uid);
        newOrder.setPid(dto.getPid());
        newOrder.setOrderStartTime(orderStartTime);
        newOrder.setOrderEndTime(orderEndTime);
        newOrder.setCreatedTime(LocalDateTime.now());
        newOrder.setUpdatedTime(LocalDateTime.now());
        newOrder.setIsDeleted("n");
        newOrder.setStatus(1);

        int result = orderMapper.insert(newOrder);

        if (result > 0) {
            // 更新用户的会员结束时间
            user.setMembershipEndTime(orderEndTime);
            user.setUpdatedTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            return ResultMsg.success(newOrder, "创建订单成功");
        } else {
            return ResultMsg.fail(null, "创建订单失败");
        }
    }

    @Override
    public ResultMsg<Order> update(OrderCreateDTO dto) {
        ResultMsg authCheck = checkUserAuth();
        if (authCheck != null) {
            return authCheck;
        }

        if (dto.getPid() == null || dto.getPid() == 0) {
            return ResultMsg.fail(null, "套餐ID不能为空");
        }

        Integer uid = getCurrentUserId();

        Package packageInfo = packageMapper.selectById(dto.getPid());
        if (packageInfo == null) {
            return ResultMsg.fail(null, "套餐不存在");
        }

        // 获取用户信息
        User user = userMapper.selectById(uid);
        if (user == null) {
            return ResultMsg.fail(null, "用户不存在");
        }

        // 检查用户是否有有效的会员资格
        LocalDate now = LocalDate.now();
        
        // TODO: 待数据库迁移后启用以下逻辑
        // if (user.getMembershipEndTime() == null || user.getMembershipEndTime().isBefore(now)) {
        //     return ResultMsg.fail(null, "未找到有效订单，请直接创建新订单");
        // }
        
        // 临时方案：查询用户是否有有效订单
        QueryWrapper<Order> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("uid", uid);
        checkWrapper.eq("is_deleted", "n");
        checkWrapper.ge("order_end_time", now);
        checkWrapper.orderByDesc("order_end_time");
        checkWrapper.last("LIMIT 1");
        Order existingOrder = orderMapper.selectOne(checkWrapper);
        
        if (existingOrder == null) {
            return ResultMsg.fail(null, "未找到有效订单，请直接创建新订单");
        }

        // 计算新的订单结束时间（从当前结束时间延续）
        LocalDate orderStartTime = now;
        LocalDate orderEndTime = existingOrder.getOrderEndTime().plusDays(packageInfo.getDuration());

        // 创建新订单记录
        Order newOrder = new Order();
        newOrder.setUid(uid);
        newOrder.setPid(dto.getPid());
        newOrder.setOrderStartTime(orderStartTime);
        newOrder.setOrderEndTime(orderEndTime);
        newOrder.setCreatedTime(LocalDateTime.now());
        newOrder.setUpdatedTime(LocalDateTime.now());
        newOrder.setIsDeleted("n");
        newOrder.setStatus(1);

        int result = orderMapper.insert(newOrder);

        if (result > 0) {
            // 更新用户的会员结束时间
            user.setMembershipEndTime(orderEndTime);
            user.setUpdatedTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            return ResultMsg.success(newOrder, "续约成功，新到期日为：" + orderEndTime);
        } else {
            return ResultMsg.fail(null, "续约失败");
        }
    }

    @Override
    public ResultMsg deleteOrder(int id) {
        try {
            checkAdminPermission();
        } catch (RuntimeException e) {
            return ResultMsg.fail(null, e.getMessage());
        }

        Order order = orderMapper.selectById(id);

        if (order == null) {
            return ResultMsg.fail(null, "订单不存在");
        }

        order.setIsDeleted("y");
        order.setUpdatedTime(LocalDateTime.now());

        int result = orderMapper.updateById(order);

        if (result > 0) {
            return ResultMsg.success(null, "删除成功");
        } else {
            return ResultMsg.fail(null, "删除失败");
        }
    }

    @Override
    public ResultMsg findAllOrders(int pageNum, int pageSize) {
        try {
            checkAdminPermission();
        } catch (RuntimeException e) {
            return ResultMsg.fail(null, e.getMessage());
        }

        Page<Order> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.orderByDesc("created_time");

        Page<Order> resultPage = orderMapper.selectPage(page, queryWrapper);

        return ResultMsg.success(resultPage, "查询成功");
    }

    @Override
    public ResultMsg findAllPackages() {
        QueryWrapper<Package> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.orderByAsc("level");

        java.util.List<Package> packages = packageMapper.selectList(queryWrapper);

        if (packages == null || packages.isEmpty()) {
            return ResultMsg.fail(null, "暂无可用套餐");
        }

        return ResultMsg.success(packages, "查询成功");
    }

    @Override
    public ResultMsg<Package> getPackageById(int id) {
        Package pkg = packageMapper.selectById(id);
        
        if (pkg == null || "y".equals(pkg.getIsDeleted())) {
            return ResultMsg.fail(null, "套餐不存在");
        }
        
        return ResultMsg.success(pkg, "查询成功");
    }

    @Override
    public ResultMsg getUserOrderHistory(int uid, int pageNum, int pageSize) {
        // 验证用户权限
        Integer currentUid = getCurrentUserId();
        if (currentUid == null) {
            return ResultMsg.fail(null, "未获取到用户信息，请通过网关访问");
        }

        // 用户只能查看自己的订单历史，管理员可以查看所有
        Integer role = getCurrentUserRole();
        if (uid!=currentUid && (role == null || role != 1)) {
            return ResultMsg.fail(null, "权限不足，仅能查看自己的订单历史");
        }

        Page<Order> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", uid);
        queryWrapper.eq("is_deleted", "n");
        queryWrapper.orderByDesc("created_time");

        Page<Order> resultPage = orderMapper.selectPage(page, queryWrapper);

        return ResultMsg.success(resultPage, "查询成功");
    }

    @Override
    public ResultMsg addPackage(Package pkg) {
        // 检查管理员权限
        checkAdminPermission();
        
        pkg.setCreatedTime(LocalDateTime.now());
        pkg.setUpdatedTime(LocalDateTime.now());
        pkg.setIsDeleted("n");
        
        int result = packageMapper.insert(pkg);
        if (result > 0) {
            return ResultMsg.success(pkg, "添加套餐成功");
        } else {
            return ResultMsg.fail(null, "添加套餐失败");
        }
    }

    @Override
    public ResultMsg updatePackage(Package pkg) {
        // 检查管理员权限
        checkAdminPermission();
        
        Package existingPackage = packageMapper.selectById(pkg.getId());
        if (existingPackage == null) {
            return ResultMsg.fail(null, "套餐不存在");
        }
        
        pkg.setUpdatedTime(LocalDateTime.now());
        int result = packageMapper.updateById(pkg);
        if (result > 0) {
            return ResultMsg.success(pkg, "更新套餐成功");
        } else {
            return ResultMsg.fail(null, "更新套餐失败");
        }
    }

    @Override
    public ResultMsg deletePackage(int id) {
        // 检查管理员权限
        checkAdminPermission();
        
        Package existingPackage = packageMapper.selectById(id);
        if (existingPackage == null) {
            return ResultMsg.fail(null, "套餐不存在");
        }
        
        existingPackage.setIsDeleted("y");
        existingPackage.setUpdatedTime(LocalDateTime.now());
        int result = packageMapper.updateById(existingPackage);
        if (result > 0) {
            return ResultMsg.success(null, "删除套餐成功");
        } else {
            return ResultMsg.fail(null, "删除套餐失败");
        }
    }


}
