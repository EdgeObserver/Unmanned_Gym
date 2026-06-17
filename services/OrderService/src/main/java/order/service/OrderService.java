package order.service;

import order.dto.OrderCreateDTO;
import pojo.Order;
import pojo.Package;
import pojo.ResultMsg;

public interface OrderService {
    ResultMsg<Order> getLegalOrderByUid(int uid);

    ResultMsg update(OrderCreateDTO dto);

    ResultMsg<Order> getOrderById(int id);

    ResultMsg createOrder(OrderCreateDTO dto);



    ResultMsg deleteOrder(int id);

    ResultMsg findAllOrders(int pageNum, int pageSize);

    ResultMsg findAllPackages();

    ResultMsg<Package> getPackageById(int id);

    ResultMsg getUserOrderHistory(int uid, int pageNum, int pageSize);
    
    /**
     * 添加套餐
     */
    ResultMsg addPackage(Package pkg);
    
    /**
     * 更新套餐
     */
    ResultMsg updatePackage(Package pkg);
    
    /**
     * 删除套餐
     */
    ResultMsg deletePackage(int id);
}
