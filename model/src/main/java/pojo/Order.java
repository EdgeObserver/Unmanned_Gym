package pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order extends Base{
    @NotNull
    private int id;
    private int uid;
    private int pid;

    private LocalDate orderStartTime;  // 订单开始时间
    private LocalDate orderEndTime;
}
