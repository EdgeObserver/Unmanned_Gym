package pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSlot extends Base {
    @NotNull
    private int id;
    private String slotName;
    private LocalTime startTime;
    private LocalTime endTime;
}
