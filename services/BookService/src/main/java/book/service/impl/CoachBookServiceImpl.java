package book.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.*;
import book.dto.CoachBookingDto;
import book.mapper.CoachBookingMapper;
import book.mapper.EquipmentBookingMapper;

import book.service.CoachBookService;


import java.time.LocalDate;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class CoachBookServiceImpl implements CoachBookService {

    @Autowired
    private CoachBookingMapper coachBookingMapper;


    @Autowired
    private EquipmentBookingMapper equipmentBookingMapper;

    @Override
    public ResultMsg bookCoach(CoachBookingDto coachBookingDto, int userId) {
        LocalDate bookingDate = coachBookingDto.getBookingDate();
        Integer slotId = coachBookingDto.getSlotId();
        Integer coachId = coachBookingDto.getResourceId();

        if (bookingDate.isBefore(LocalDate.now())) {
            return ResultMsg.fail(null, "不能预约过去的日期");
        }

        QueryWrapper<CoachBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coach_id", coachId)
                .eq("booking_date", bookingDate)
                .eq("slot_id", slotId)
                .eq("status", 1)
                .eq("is_deleted", "n");

        Long count = coachBookingMapper.selectCount(queryWrapper);
        if (count > 0) {
            return ResultMsg.fail(null, "该时间段已被预约");
        }

        CoachBooking booking = new CoachBooking();
        booking.setUserId(userId);
        booking.setCoachId(coachId);
        booking.setBookingDate(bookingDate);
        booking.setSlotId(slotId);
        booking.setStatus(1);
        booking.setIsDeleted("n");

        coachBookingMapper.insert(booking);

        return ResultMsg.success(booking, "预约教练成功");
    }

    @Override
    public ResultMsg cancelCoachBooking(int bookingId, int userId) {
        CoachBooking booking = coachBookingMapper.selectById(bookingId);

        if (booking == null || "y".equals(booking.getIsDeleted())) {
            return ResultMsg.fail(null, "预约记录不存在");
        }

        if (booking.getUserId() != userId) {
            return ResultMsg.fail(null, "无权取消此预约");
        }

        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            return ResultMsg.fail(null, "不能取消已过期的预约");
        }

        booking.setStatus(0);
        coachBookingMapper.updateById(booking);

        return ResultMsg.success(null, "取消预约成功");
    }

    @Override
    public ResultMsg getCoachBookings(int coachId, String date) {
        QueryWrapper<CoachBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coach_id", coachId)
                .eq("status", 1)
                .eq("is_deleted", "n");

        if (date != null && !date.isEmpty()) {
            queryWrapper.eq("booking_date", date);
        }

        List<CoachBooking> bookings = coachBookingMapper.selectList(queryWrapper);
        return ResultMsg.success(bookings, "查询成功");
    }

    @Override
    public ResultMsg getUserCoachBookings(int userId) {
        QueryWrapper<CoachBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_deleted", "n")
                .orderByDesc("booking_date");

        List<CoachBooking> bookings = coachBookingMapper.selectList(queryWrapper);
        return ResultMsg.success(bookings, "查询成功");
    }

    @Override
    public ResultMsg getAllCoachBookings(int pageNum, int pageSize) {
        Page<CoachBooking> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CoachBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n")
                .orderByDesc("created_time");
        
        Page<CoachBooking> resultPage = coachBookingMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(resultPage, "查询成功");
    }
}
