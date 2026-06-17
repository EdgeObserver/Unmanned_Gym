package book.service.impl;
import book.feign.ResourceClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.*;
import book.dto.EquipmentBookingDto;
import book.mapper.CoachBookingMapper;
import book.mapper.EquipmentBookingMapper;

import book.service.EquipmentBookService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class EquipmentBookServiceImpl implements EquipmentBookService {

    @Autowired
    private EquipmentBookingMapper equipmentBookingMapper;

    @Autowired
    private CoachBookingMapper coachBookingMapper;

    @Autowired
    private ResourceClient resourceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResultMsg bookEquipment(EquipmentBookingDto request, int userId) {
        LocalDate bookingDate = request.getBookingDate();
        Integer slotId = request.getSlotId();
        Integer equipmentId = request.getResourceId();

        if (bookingDate.isBefore(LocalDate.now())) {
            return ResultMsg.fail(null, "不能预约过去的日期");
        }

        ResultMsg equipmentResult = resourceClient.getEquipmentById(equipmentId);
        if (equipmentResult.getCode() != 200 || equipmentResult.getData() == null) {
            return ResultMsg.fail(null, "器材不存在");
        }

        Equipment equipment = objectMapper.convertValue(equipmentResult.getData(), Equipment.class);

        QueryWrapper<EquipmentBooking> userSlotQuery = new QueryWrapper<>();
        userSlotQuery.eq("user_id", userId)
                .eq("booking_date", bookingDate)
                .eq("slot_id", slotId)
                .eq("equipment_id", equipmentId)
                .eq("status", 1)
                .eq("is_deleted", "n");

        Long userExistingCount = equipmentBookingMapper.selectCount(userSlotQuery);
        if (userExistingCount > 0) {
            return ResultMsg.fail(null, "您在此时间段已预约过该器材");
        }

        QueryWrapper<EquipmentBooking> userDayQuery = new QueryWrapper<>();
        userDayQuery.eq("user_id", userId)
                .eq("booking_date", bookingDate)
                .eq("status", 1)
                .eq("is_deleted", "n");
        Long userDayCount = equipmentBookingMapper.selectCount(userDayQuery);
        if (userDayCount >= 2) {
            return ResultMsg.fail(null, "每天最多只能预约2个器材");
        }

        QueryWrapper<EquipmentBooking> slotQuery = new QueryWrapper<>();
        slotQuery.eq("equipment_id", equipmentId)
                .eq("booking_date", bookingDate)
                .eq("slot_id", slotId)
                .eq("status", 1)
                .eq("is_deleted", "n");
        Long bookedCount = equipmentBookingMapper.selectCount(slotQuery);

        if (bookedCount >= equipment.getNum()) {
            return ResultMsg.fail(null, "该器材在此时间段已全部被预约");
        }

        if (equipment.getNeedCoach() == 1) {
            QueryWrapper<CoachBooking> coachQuery = new QueryWrapper<>();
            coachQuery.eq("user_id", userId)
                    .eq("booking_date", bookingDate)
                    .eq("slot_id", slotId)
                    .eq("status", 1)
                    .eq("is_deleted", "n");

            Long coachCount = coachBookingMapper.selectCount(coachQuery);
            if (coachCount == 0) {
                return ResultMsg.fail(null, "该器材需要教练指导，请先预约同一时间段的教练");
            }
        }

        EquipmentBooking booking = new EquipmentBooking();
        booking.setUserId(userId);
        booking.setEquipmentId(equipmentId);
        booking.setBookingDate(bookingDate);
        booking.setSlotId(slotId);
        booking.setStatus(1);
        booking.setIsDeleted("n");

        equipmentBookingMapper.insert(booking);

        long remaining = equipment.getNum() - bookedCount - 1;
        return ResultMsg.success(booking, "预约器材成功，该时间段剩余数量：" + remaining);
    }

    @Override
    public ResultMsg cancelEquipmentBooking(int bookingId, int userId) {
        EquipmentBooking booking = equipmentBookingMapper.selectById(bookingId);

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
        equipmentBookingMapper.updateById(booking);

        return ResultMsg.success(null, "取消预约成功");
    }

    @Override
    public ResultMsg getEquipmentBookings(int equipmentId, String date) {
        QueryWrapper<EquipmentBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("equipment_id", equipmentId)
                .eq("status", 1)
                .eq("is_deleted", "n");

        if (date != null && !date.isEmpty()) {
            queryWrapper.eq("booking_date", date);
        }

        List<EquipmentBooking> bookings = equipmentBookingMapper.selectList(queryWrapper);
        return ResultMsg.success(bookings, "查询成功");
    }

    @Override
    public ResultMsg getUserEquipmentBookings(int userId) {
        QueryWrapper<EquipmentBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_deleted", "n")
                .orderByDesc("booking_date");

        List<EquipmentBooking> bookings = equipmentBookingMapper.selectList(queryWrapper);
        return ResultMsg.success(bookings, "查询成功");
    }

    @Override
    public ResultMsg getAllEquipmentBookings(int pageNum, int pageSize) {
        Page<EquipmentBooking> page = new Page<>(pageNum, pageSize);
        QueryWrapper<EquipmentBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", "n")
                .orderByDesc("created_time");
        
        Page<EquipmentBooking> resultPage = equipmentBookingMapper.selectPage(page, queryWrapper);
        return ResultMsg.success(resultPage, "查询成功");
    }
}
