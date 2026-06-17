package record.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_FACE_EVENT = "face.event.exchange";
    
    // RecordService 专用的队列
    public static final String QUEUE_RECORD_CHECKIN = "record.checkin.queue";
    public static final String QUEUE_RECORD_CHECKOUT = "record.checkout.queue";

    public static final String ROUTING_KEY_CHECKIN = "face.checkin";
    public static final String ROUTING_KEY_CHECKOUT = "face.checkout";

    /**
     * 声明交换机
     */
    @Bean
    public TopicExchange faceEventExchange() {
        return new TopicExchange(EXCHANGE_FACE_EVENT, true, false);
    }
    
    /**
     * 声明RecordService专用的签到队列
     */
    @Bean
    public Queue recordCheckInQueue() {
        return QueueBuilder.durable(QUEUE_RECORD_CHECKIN).build();
    }
    
    /**
     * 声明RecordService专用的签退队列
     */
    @Bean
    public Queue recordCheckOutQueue() {
        return QueueBuilder.durable(QUEUE_RECORD_CHECKOUT).build();
    }
    
    /**
     * 绑定签到队列到交换机
     */
    @Bean
    public Binding recordCheckInBinding(Queue recordCheckInQueue, TopicExchange faceEventExchange) {
        return BindingBuilder.bind(recordCheckInQueue)
                .to(faceEventExchange)
                .with(ROUTING_KEY_CHECKIN);
    }
    
    /**
     * 绑定签退队列到交换机
     */
    @Bean
    public Binding recordCheckOutBinding(Queue recordCheckOutQueue, TopicExchange faceEventExchange) {
        return BindingBuilder.bind(recordCheckOutQueue)
                .to(faceEventExchange)
                .with(ROUTING_KEY_CHECKOUT);
    }
}
