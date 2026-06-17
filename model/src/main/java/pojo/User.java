package pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
public class User extends Base {
    @NotNull
    @TableId(type = IdType.AUTO)
    private int id;
    private String username;
    private String password;
    @Column(columnDefinition = "tinyint(1)")
    private boolean inPlace;
    @Email
    private String email;
    private String userPic;
    private LocalDate membershipEndTime; // 会员结束时间

}

