package record.controller;


import record.service.ActionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.RecordPageParam;
import pojo.ResultMsg;

@RestController
public class ActionServiceController {

    @Autowired
    private ActionRecordService actionRecordService;

    // 创建新的ActionRecord
    @PostMapping("/arrive")
    public ResultMsg arrive(@RequestParam int id) {
        return actionRecordService.arrive(id);

    }

    // 根据ID获取ActionRecord
    @GetMapping("/get")
    public ResultMsg getActionRecordById(@RequestParam int id) {
        return actionRecordService.getActionRecordById(id);

    }

    // 获取所有ActionRecords
    @GetMapping("/page")
    public ResultMsg getList(@RequestParam int pageNum, @RequestParam int pageSize) {
        return actionRecordService.findAllPage(pageNum, pageSize);

    }

    // 删除ActionRecord
    @DeleteMapping("/delete")
    public ResultMsg deleteActionRecord(@RequestParam int id) {
        return actionRecordService.deleteActionRecordById(id);
    }
    @PostMapping("/leave")
    public ResultMsg leave(@RequestParam int id) {
        return actionRecordService.leave(id);
    }
    @PostMapping("/list")
    public ResultMsg getList(@RequestBody RecordPageParam recordPageParam) {
        return actionRecordService.getList(recordPageParam);
    }
    
    /**
     * 获取今日访问人数
     */
    @GetMapping("/today-count")
    public ResultMsg getTodayVisitCount() {
        return actionRecordService.getTodayVisitCount();
    }
    
    /**
     * 获取近7天访问统计
     */
    @GetMapping("/weekly-stats")
    public ResultMsg getWeeklyVisitStatistics() {
        return actionRecordService.getWeeklyVisitStatistics();
    }
}
