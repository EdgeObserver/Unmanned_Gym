package book.service;

import pojo.ResultMsg;
import book.dto.CoachBookingDto;

public interface CoachBookService {
    ResultMsg bookCoach(CoachBookingDto coachBookingDto, int userId);

    ResultMsg cancelCoachBooking(int bookingId, int userId);

    ResultMsg getCoachBookings(int coachId, String date);

    ResultMsg getUserCoachBookings(int userId);
    
    /**
     * 获取所有教练预约（管理员）
     */
    ResultMsg getAllCoachBookings(int pageNum, int pageSize);
}
