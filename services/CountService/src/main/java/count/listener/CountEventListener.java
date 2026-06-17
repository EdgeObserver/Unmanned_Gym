package count.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import count.serivce.CountService;
import face.dto.FaceEventMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CountEventListener {

    @Autowired
    private CountService countService;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = "face.checkin.queue")
    public void handleCheckIn(Message message) {
        try {
            String body = new String(message.getBody());
            FaceEventMessage event = objectMapper.readValue(body, FaceEventMessage.class);

            System.out.println("[RabbitMQ] 收到签到消息: userId=" + event.getUserId());
            countService.arrive();
            int currentCount = countService.getCurrentCount().getData();
            System.out.println("[RabbitMQ] ✅ 签到成功，当前应有人数: " + currentCount);

        } catch (Exception e) {
            System.err.println("[RabbitMQ] ❌ 处理签到消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = "face.checkout.queue")
    public void handleCheckOut(Message message) {
        try {
            String body = new String(message.getBody());
            FaceEventMessage event = objectMapper.readValue(body, FaceEventMessage.class);

            System.out.println("[RabbitMQ] 收到签退消息: userId=" + event.getUserId());
            countService.leave();
            int currentCount = countService.getCurrentCount().getData();
            System.out.println("[RabbitMQ] ✅ 签退成功，当前应有人数: " + currentCount);

        } catch (Exception e) {
            System.err.println("[RabbitMQ] ❌ 处理签退消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
