package pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EquipmentBooking extends Base {
    @NotNull
    private int id;
    private int userId;
    private int equipmentId;
    private LocalDate bookingDate;
    private int slotId;

}
