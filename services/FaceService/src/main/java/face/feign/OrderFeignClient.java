package face.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pojo.Order;
import pojo.ResultMsg;

@FeignClient(name = "OrderService")
public interface OrderFeignClient {
    @GetMapping("/getLegalOrderbyUid")
    public ResultMsg<Order> getLegalOrderbyUid(@RequestParam("uid") int uid);
    @PostMapping("/update")
    public ResultMsg update(Order order);
}
