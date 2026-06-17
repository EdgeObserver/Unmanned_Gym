package count.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_FACE_EVENT = "face.event.exchange";

    /**
     * 只声明交换机，不声明队列
     * 队列由FaceService统一声明，这里只需要确保交换机存在即可
     */
    @Bean
    public TopicExchange faceEventExchange() {
        return new TopicExchange(EXCHANGE_FACE_EVENT, true, false);
    }
}
