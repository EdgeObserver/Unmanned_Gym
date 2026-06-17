package book.service;

import pojo.ResultMsg;
import book.dto.EquipmentBookingDto;

public interface EquipmentBookService {
    ResultMsg bookEquipment( EquipmentBookingDto equipmentBookingDto, int userId);

    ResultMsg cancelEquipmentBooking(int bookingId, int userId);

    ResultMsg getEquipmentBookings(int equipmentId, String date);

    ResultMsg getUserEquipmentBookings(int userId);
    
    /**
     * 获取所有器材预约（管理员）
     */
    ResultMsg getAllEquipmentBookings(int pageNum, int pageSize);
}
