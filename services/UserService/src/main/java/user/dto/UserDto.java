package user.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserDto implements Serializable {


    /* 修改资料时带主键（注册时可空） */
    private Integer id;

    /* 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度 3-20 位")
    private String username;
    @Size(min = 5, max = 20, message = "密码长度 3-20 位")
    private String password;
    /* 场地状态（可选，默认 false） */
    private Boolean inPlace;

    /* 邮箱 */
    @Email(message = "邮箱格式不正确")
    @NotBlank(message ="邮箱不能为空")
    private String email;

    /* 套餐ID（注册时必填） */
    private Integer packageId;

}