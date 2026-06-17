package face.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pojo.ResultMsg;

@FeignClient(name = "CountService")
public interface CountFeignService {
    @GetMapping("/current")
    public ResultMsg<Integer> getCurrentCount();

    /**
     * 人员离开
     */
    @PostMapping("/leave")
    public ResultMsg<String> leave() ;

    /**
     * 人员到达
     */
    @PostMapping("/arrive")
    public ResultMsg<String> arrive();

    /**
     * 检查统计
     * @param fresh 刷新参数
     */
    @PostMapping("/check")
    public ResultMsg<Integer> checkCount(@RequestParam int fresh);
}
