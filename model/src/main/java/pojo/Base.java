package pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
@Data
public class Base {
    private String createdBy;
    private LocalDateTime createdTime;
    private String updatedBy;
    private LocalDateTime updatedTime;
    private String isDeleted;
    private Integer status;
}
