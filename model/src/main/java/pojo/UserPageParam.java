package pojo;


import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigInteger;

@Getter
@Data
public class UserPageParam implements Serializable {
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
    private String name;
    private BigInteger roleId;

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoleId(BigInteger roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "SysUserPageParam:{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", roleId=" + roleId +
                '}';
    }
}
