package pojo;



import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class RecordPageParam implements Serializable {
    /*
    const pageParam=reactive({
	pageNum:1,//当前页
	pageSize:2,//每页条数
	userName:'',
	realName:'',
	roleId:''
})

     */
    private Integer pageNum;
    private Integer pageSize;
    private String userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    @Override
    public String toString() {
        return "SysUserPageParam:{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", userId='" + userId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime=" + endTime +
                '}';
    }
}
