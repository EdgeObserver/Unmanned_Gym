package com.AttendanceSystem.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Order {
    @NotNull
    private Integer id;
    @NotNull
    private Integer user_id;
    @NotNull
    private Integer package_id;
    private boolean is_valid;
    private String isDeleted;
    private Date create_time;
    private Date expire_time;

}
