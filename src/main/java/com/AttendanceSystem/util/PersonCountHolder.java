package com.AttendanceSystem.util;

import com.AttendanceSystem.pojo.ResultMsg;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PersonCountHolder {
    @Getter
    @Setter
    private volatile int current = 0;          // 当前在场人数
    private final int threshold = 3;           // 连续满足几次才触发
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 每次拿到新人数后调用
     *

     * @return true=应该发出提示
     */
    public ResultMsg<Integer> arrive(int fresh) {

        if (fresh > current) {
            if (counter.incrementAndGet() >= threshold) {
//                System.out.println("warn");
                return ResultMsg.fail(current,"人数异常");
            }
        } else {                                // 不满足条件就清零
            counter.set(0);
        }
        return ResultMsg.success(current,"人数正常");
    }
}