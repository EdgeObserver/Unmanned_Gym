package face.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pojo.ResultMsg;

@FeignClient(name = "RecordService")
public interface ActionRecordFeignClient {
    @PostMapping("/arrive")
    public ResultMsg arrive(@RequestParam int id);
    @PostMapping("/leave")
    public ResultMsg leave(@RequestParam int id);
}
