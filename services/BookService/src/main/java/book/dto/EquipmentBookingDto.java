package book.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EquipmentBookingDto {
    @NotNull(message = "预约日期不能为空")
    private LocalDate bookingDate;

    @NotNull(message = "时间段ID不能为空")
    private Integer slotId;

    @NotNull(message = "资源ID不能为空")
    private Integer resourceId;

    private Integer bookingType;
}
