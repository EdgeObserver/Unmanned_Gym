package pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
@Data
public class Manager extends Base {
    @NotNull
    @TableId(type = IdType.AUTO)
    private int id;
    private String username;
    private String password;

}

