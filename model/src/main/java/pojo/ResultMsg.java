package pojo;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ResultMsg<T> implements Serializable {

    private Integer code;

    private String msg;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private T data;

    public static <T> ResultMsg<T> success(T data, String msg){
        ResultMsg<T> resultMsg = new ResultMsg<>();
        resultMsg.setCode(200);
        resultMsg.setMsg(msg);
        resultMsg.setData(data);
        return resultMsg;
    }

    public static <T> ResultMsg<T> fail(T data, String msg){
        ResultMsg<T> resultMsg = new ResultMsg<>();
        resultMsg.setCode(500);
        resultMsg.setMsg(msg);
        resultMsg.setData(data);
        return resultMsg;
    }

}
