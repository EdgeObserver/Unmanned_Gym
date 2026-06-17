package record.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import face.dto.FaceEventMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import record.config.RabbitMQConfig;
import record.service.ActionRecordService;

@Component
public class FaceEventListener {

    @Autowired
    private ActionRecordService actionRecordService;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECORD_CHECKIN)
    public void handleCheckIn(Message message) {
        try {
            String body = new String(message.getBody());
            System.out.println("========== RecordService 收到签到消息 ==========");
            System.out.println("消息内容: " + body);
            
            FaceEventMessage event = objectMapper.readValue(body, FaceEventMessage.class);
            
            System.out.println("用户ID: " + event.getUserId());
            System.out.println("用户名: " + event.getUsername());
            System.out.println("事件类型: " + event.getEventType());
            System.out.println("时间戳: " + event.getTimestamp());

            actionRecordService.arrive(event.getUserId());

            System.out.println("处理签到事件成功");
            System.out.println("==================================");

        } catch (Exception e) {
            System.err.println("处理签到消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_RECORD_CHECKOUT)
    public void handleCheckOut(Message message) {
        try {
            String body = new String(message.getBody());
            System.out.println("========== RecordService 收到签退消息 ==========");
            System.out.println("消息内容: " + body);
            
            FaceEventMessage event = objectMapper.readValue(body, FaceEventMessage.class);
            
            System.out.println("用户ID: " + event.getUserId());
            System.out.println("用户名: " + event.getUsername());
            System.out.println("事件类型: " + event.getEventType());
            System.out.println("时间戳: " + event.getTimestamp());

            actionRecordService.leave(event.getUserId());

            System.out.println("处理签退事件成功");
            System.out.println("==================================");

        } catch (Exception e) {
            System.err.println("处理签退消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
