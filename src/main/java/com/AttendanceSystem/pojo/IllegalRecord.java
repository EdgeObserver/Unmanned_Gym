package com.AttendanceSystem.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IllegalRecord {
    @NotNull
    int id;
    private String isDeleted;
    private int real_count;
    private int legal_count;
    private LocalDateTime time;
}
