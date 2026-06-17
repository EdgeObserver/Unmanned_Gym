package count.serivce;

import org.springframework.scheduling.annotation.Scheduled;
import pojo.ResultMsg;

public interface CountService {
    public ResultMsg<Integer> getCurrentCount();
    public ResultMsg<String> leave();
    public ResultMsg<String> arrive();
    public ResultMsg<Integer> checkCount(int fresh);
    @Scheduled(fixedDelay = 500)
    void pushColor();
}
