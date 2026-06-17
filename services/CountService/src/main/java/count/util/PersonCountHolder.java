package count.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import pojo.ResultMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PersonCountHolder {
    @Getter
    @Setter
    private volatile int current = 0;          // 当前在场人数
    private final int threshold = 1;           // 连续满足几次才触发（改为1便于测试）
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 每次拿到新人数后调用
     *

     * @return true=应该发出提示
     */
    public ResultMsg<Integer> checkCount(int fresh) {
        System.out.println("[人数检测] 摄像头检测到: " + fresh + ", 应有人数: " + current);
        
        if (fresh > current) {
            int newCounter = counter.incrementAndGet();
            System.out.println("[人数检测] ⚠️ 检测到人数超标! " + fresh + " > " + current + ", 计数器: " + newCounter + "/" + threshold);
            if (newCounter >= threshold) {
                System.out.println("[人数检测] 🔴 触发警报，推送红色信号!");
                return ResultMsg.fail(current,"人数异常");
            }
        } else {
            if (counter.get() > 0) {
                System.out.println("[人数检测] ✅ 人数正常，重置计数器");
            }
            counter.set(0);
        }
        return ResultMsg.success(current,"人数正常");
    }
}