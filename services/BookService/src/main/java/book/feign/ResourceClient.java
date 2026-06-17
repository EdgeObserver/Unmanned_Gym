package book.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pojo.ResultMsg;

@FeignClient(name = "ResourceService")
public interface ResourceClient {
    @GetMapping("/equipment/get")
    public ResultMsg getEquipmentById(@RequestParam int id);

}
