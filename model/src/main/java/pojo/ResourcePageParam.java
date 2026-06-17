package pojo;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;

@Setter
@Getter
@Data
public class ResourcePageParam implements Serializable {
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

    @Override
    public String toString() {
        return "SysUserPageParam:{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}
