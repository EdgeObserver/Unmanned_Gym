package com.AttendanceSystem.pojo;


/*
这个类是所有实体类的弗雷
公共的属性全部放在这里
 */

import lombok.Data;

import java.util.Date;
@Data
public class Base {
    private Date createdTime;
    private Date updatedTime;
    private String isDeleted;
    private Integer status;

//    public Integer getStatus() {
//        return status;
//    }
//
//    public void setStatus(Integer status) {
//        this.status = status;
//    }
//
//    public String getIsDeleted() {
//        return isDeleted;
//    }
//
//    public void setIsDeleted(String isDeleted) {
//        this.isDeleted = isDeleted;
//    }
//
//    public Date getUpdatedTime() {
//        return updatedTime;
//    }
//
//    public void setUpdatedTime(Date updatedTime) {
//        this.updatedTime = updatedTime;
//    }
//
//
//
//    public Date getCreatedTime() {
//        return createdTime;
//    }
//
//    public void setCreatedTime(Date createdTime) {
//        this.createdTime = createdTime;
//    }


}
