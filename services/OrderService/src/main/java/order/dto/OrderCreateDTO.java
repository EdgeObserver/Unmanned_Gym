package order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull(message = "套餐ID不能为空")
    private Integer pid;
}
