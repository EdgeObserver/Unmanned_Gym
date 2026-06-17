package user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pojo.Order;
import pojo.Package;
import pojo.ResultMsg;
import user.config.FeignConfig;

@FeignClient(name = "OrderService", configuration = FeignConfig.class)
public interface OrderClient {
    @GetMapping("/getLegalOrderbyUid")
    public ResultMsg<Order> getLegalOrderbyUid(@RequestParam("uid") int uid);

    @PostMapping("/update")
    public ResultMsg update(@RequestBody Order order);

    @GetMapping("/package/get")
    public ResultMsg<Package> getPackageById(@RequestParam("id") int id);
}
