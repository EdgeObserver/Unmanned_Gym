package pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CoachBooking extends Base {
    @NotNull
    private int id;
    private int userId;
    private int coachId;
    private LocalDate bookingDate;
    private int slotId;
}
