package com.AttendanceSystem.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ActionRecord {
    @NotNull
    private int id;
    private int uid;
    private String isDeleted;
    private boolean status;
    private LocalDateTime arrive_time;
    private LocalDateTime leave_time;
}
