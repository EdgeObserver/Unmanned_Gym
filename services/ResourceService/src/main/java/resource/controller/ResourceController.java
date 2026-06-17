package resource.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.Coach;
import pojo.ResultMsg;
import pojo.Equipment;
import resource.dto.CoachDto;
import resource.dto.ResourceDto;
import resource.service.impl.CoachServiceImpl;
import resource.service.impl.EquipmentServiceImpl;

@RestController
public class ResourceController {

    @Autowired
    private EquipmentServiceImpl reserveService;

    @Autowired
    private CoachServiceImpl coachService;



    // 查询所有资源
    @GetMapping("/equipment/all")
    public ResultMsg findAllResource() {
        return reserveService.findAllResource();
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping("/equipment/page")
    public ResultMsg findAllResourcePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return reserveService.findAllResourcePage(pageNum, pageSize);
    }

    // 根据ID查询资源
    @GetMapping("/equipment/get")
    public ResultMsg getResourceById(@RequestParam int id) {
        return reserveService.getResourceById(id);

    }

    // 添加资源
    @PostMapping("/equipment/add")
    public ResultMsg addResource(@RequestBody ResourceDto resourceDto) {
        return reserveService.addResource(resourceDto);
    }

    // 更新资源
    @PutMapping("/equipment/update")
    public ResultMsg updateResource(@RequestBody Equipment equipment) {
        return reserveService.updateResource(equipment);
    }

    // 删除资源
    @DeleteMapping("/equipment/delete")
    public ResultMsg deleteResource(@RequestParam int id) {
        return reserveService.deleteResource(id);
    }

    @GetMapping("/coach/all")
    public ResultMsg findAllCoach() {
        return coachService.findAllCoach();
    }

    @GetMapping("/coach/page")
    public ResultMsg findAllCoachPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return coachService.findAllCoachPage(pageNum, pageSize);
    }

    @GetMapping("/coach/get")
    public ResultMsg getCoachById(@RequestParam int id) {
        return coachService.getCoachById(id);
    }

    @PostMapping("/coach/add")
    public ResultMsg addCoach(@RequestBody CoachDto coachDto) {
        return coachService.addCoach(coachDto);
    }

    @PutMapping("/coach/update")
    public ResultMsg updateCoach(@RequestBody Coach coach) {
        return coachService.updateCoach(coach);
    }

    @DeleteMapping("/coach/delete")
    public ResultMsg deleteCoach(@RequestParam int id) {
        return coachService.deleteCoach(id);
    }

    @GetMapping("/timeslot/all")
    public ResultMsg findAllTimeSlots() {
        return coachService.findAllTimeSlots();
    }
}
