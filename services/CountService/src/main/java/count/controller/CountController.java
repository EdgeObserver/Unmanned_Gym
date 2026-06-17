package count.controller;

import count.serivce.CountService;
import count.serivce.impl.CountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import pojo.ResultMsg;

@RestController
public class CountController {

    @Autowired
    private CountServiceImpl countService;

    /**
     * 获取当前人数
     */
    @GetMapping("/current")
    public ResultMsg<Integer> getCurrentCount() {
        return countService.getCurrentCount();
    }

    /**
     * 人员离开
     */
    @PostMapping("/leave")
    public ResultMsg<String> leave() {
        return countService.leave();
    }

    /**
     * 人员到达
     */
    @PostMapping("/arrive")
    public ResultMsg<String> arrive() {
        return countService.arrive();
    }

    /**
     * 检查统计
     * @param fresh 刷新参数
     */
    @PostMapping("/check")
    public ResultMsg<Integer> checkCount(@RequestParam int fresh) {
        return countService.checkCount(fresh);
    }
}
