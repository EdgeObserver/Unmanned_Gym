package book.controller;
import book.dto.CoachBookingDto;
import book.dto.EquipmentBookingDto;
import book.service.impl.CoachBookServiceImpl;
import book.service.impl.EquipmentBookServiceImpl;
import book.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.ResultMsg;


import java.util.Map;

@RestController

public class BookController {

    @Autowired
    private CoachBookServiceImpl coachBookService;

    @Autowired
    private EquipmentBookServiceImpl equipmentBookService;

    @PostMapping("/coach")
    public ResultMsg bookCoach(@RequestBody CoachBookingDto request) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        int userId = (Integer) claims.get("id");
        return coachBookService.bookCoach(request, userId);
    }

    @DeleteMapping("/coach/{bookingId}")
    public ResultMsg cancelCoachBooking(@PathVariable int bookingId) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        int userId = (Integer) claims.get("id");
        return coachBookService.cancelCoachBooking(bookingId, userId);
    }

    @GetMapping("/coach/list")
    public ResultMsg getCoachBookings(@RequestParam int coachId,
                                      @RequestParam(required = false) String date) {
        return coachBookService.getCoachBookings(coachId, date);
    }

    @GetMapping("/coach/my")
    public ResultMsg getMyCoachBookings() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        int userId = (Integer) claims.get("id");
        return coachBookService.getUserCoachBookings(userId);
    }

    @PostMapping("/equipment")
    public ResultMsg bookEquipment(@RequestBody EquipmentBookingDto request) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        int userId = (Integer) claims.get("id");
        return equipmentBookService.bookEquipment(request, userId);
    }

    @DeleteMapping("/equipment/{bookingId}")
    public ResultMsg cancelEquipmentBooking(@PathVariable int bookingId, HttpServletRequest httpRequest) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        int userId = (Integer) claims.get("id");
        return equipmentBookService.cancelEquipmentBooking(bookingId, userId);
    }

    @GetMapping("/equipment/list")
    public ResultMsg getEquipmentBookings(@RequestParam int equipmentId,
                                          @RequestParam(required = false) String date) {
        return equipmentBookService.getEquipmentBookings(equipmentId, date);
    }

    @GetMapping("/equipment/my")
    public ResultMsg getMyEquipmentBookings() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        int userId = (Integer) claims.get("id");
        return equipmentBookService.getUserEquipmentBookings(userId);
    }
    
    /**
     * 获取所有器材预约（管理员）
     */
    @GetMapping("/equipment/all")
    public ResultMsg getAllEquipmentBookings(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        return equipmentBookService.getAllEquipmentBookings(pageNum, pageSize);
    }
    
    /**
     * 获取所有教练预约（管理员）
     */
    @GetMapping("/coach/all")
    public ResultMsg getAllCoachBookings(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize) {
        return coachBookService.getAllCoachBookings(pageNum, pageSize);
    }
}
