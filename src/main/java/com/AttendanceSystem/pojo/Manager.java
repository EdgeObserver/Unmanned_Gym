package com.AttendanceSystem.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

public class Manager {
    @NotNull
    private Integer id;//主键ID
    private String username;//用户名
    @JsonIgnore//让springmvc把当前对象转换成json字符串的时候,忽略password,最终的json字符串中就没有password这个属性了
    private String password;//密码
}
