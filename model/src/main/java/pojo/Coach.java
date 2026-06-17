package pojo;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Coach extends Base{
    @NotNull
    private Integer id;//主键ID
    private String name;//用户名
    @Email
    private String email;//邮箱
    private String userPic;//用户头像地址
}
