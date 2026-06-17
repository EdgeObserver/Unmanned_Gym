package com.AttendanceSystem.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Package {
    @NotNull
    private Integer id;
    private Integer days;
    private Integer level;
}
