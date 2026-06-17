package resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class CoachDto {
    private Integer id;

    /* 用户名 */
    @NotBlank(message = "姓名不能为空")
    @Size(min = 1, max = 20, message = "姓名1-20 位")
    private String name;

}
