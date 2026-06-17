package face.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pojo.ResultMsg;
import pojo.User;

@FeignClient(name = "UserService")
public interface UserFeignClient {

    @GetMapping("/getById")
    public ResultMsg<User> getById(@RequestParam int id);
    @PutMapping("/update")
    public ResultMsg update(@RequestBody User user);
}
