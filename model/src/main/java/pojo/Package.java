package pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Package extends Base{
    @NotNull
    private int id;
    private String Name;
    private int price;
    private int level;
    private int duration;
}
