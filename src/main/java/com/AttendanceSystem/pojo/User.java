package com.AttendanceSystem.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User extends Base implements Serializable {
    @NotNull
    @TableId(type = IdType.AUTO)
    private Integer id;//主键ID
    private String username;//用户名
    @Column(columnDefinition = "tinyint(1)")
    private boolean inPlace;//是否正在场地
    @Email
    private String email;//邮箱
    private String userPic;//用户头像地址
}
