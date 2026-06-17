package resource.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

@Data
@Getter
public class ResourceDto implements Serializable {


    /* 修改资料时带主键（注册时可空） */
    private Integer id;

    /* 用户名 */
    @NotBlank(message = "器材名称不能为空")
    @Size(min = 1, max = 20, message = "器材名称长度 1-20 位")
    private String name;
    private int num;
    private int needCoach;

}