package pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ActionRecord extends Base{
    @NotNull
    private int id;
    @NotNull
    private int userId;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
}
