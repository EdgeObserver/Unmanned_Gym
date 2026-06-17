package pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Equipment extends Base{
    @NotNull
    private int id;
    private String name;
    private int num;
    private int needCoach;
    private int spare;
}
