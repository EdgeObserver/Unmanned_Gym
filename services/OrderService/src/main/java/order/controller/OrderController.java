package order.controller;

import order.dto.OrderCreateDTO;
import order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.Order;
import pojo.ResultMsg;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/getLegalOrderbyUid")
    public ResultMsg<Order> getLegalOrderByUid(@RequestParam int uid) {
        return orderService.getLegalOrderByUid(uid);
    }

    @PostMapping("/update")
    public ResultMsg update(@RequestBody OrderCreateDTO dto) {
        return orderService.update(dto);
    }

    @GetMapping("/get")
    public ResultMsg<Order> getOrderById(@RequestParam int id) {
        return orderService.getOrderById(id);
    }

    @PostMapping("/create")
    public ResultMsg createOrder(@RequestBody OrderCreateDTO dto) {
        return orderService.createOrder(dto);
    }

    @DeleteMapping("/delete")
    public ResultMsg deleteOrder(@RequestParam int id) {
        return orderService.deleteOrder(id);
    }

    // ... existing code ...

    @GetMapping("/page")
    public ResultMsg findAllOrders(@RequestParam int pageNum, @RequestParam int pageSize) {
        return orderService.findAllOrders(pageNum, pageSize);
    }

    @GetMapping("/package/all")
    public ResultMsg findAllPackages() {
        return orderService.findAllPackages();
    }

    @GetMapping("/package/get")
    public ResultMsg<pojo.Package> getPackageById(@RequestParam int id) {
        return orderService.getPackageById(id);
    }

    @GetMapping("/history")
    public ResultMsg getUserOrderHistory(@RequestParam int uid, @RequestParam int pageNum, @RequestParam int pageSize) {
        return orderService.getUserOrderHistory(uid, pageNum, pageSize);
    }
    
    /**
     * 添加套餐（管理员）
     */
    @PostMapping("/package/add")
    public ResultMsg addPackage(@RequestBody pojo.Package pkg) {
        return orderService.addPackage(pkg);
    }
    
    /**
     * 更新套餐（管理员）
     */
    @PutMapping("/package/update")
    public ResultMsg updatePackage(@RequestBody pojo.Package pkg) {
        return orderService.updatePackage(pkg);
    }
    
    /**
     * 删除套餐（管理员）
     */
    @DeleteMapping("/package/delete")
    public ResultMsg deletePackage(@RequestParam int id) {
        return orderService.deletePackage(id);
    }
}

