package face.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_FACE_EVENT = "face.event.exchange";

    public static final String QUEUE_FACE_CHECKIN = "face.checkin.queue";
    public static final String QUEUE_FACE_CHECKOUT = "face.checkout.queue";

    public static final String ROUTING_KEY_CHECKIN = "face.checkin";
    public static final String ROUTING_KEY_CHECKOUT = "face.checkout";

    @Bean
    public TopicExchange faceEventExchange() {
        return new TopicExchange(EXCHANGE_FACE_EVENT, true, false);
    }

    @Bean
    public Queue checkInQueue() {
        return QueueBuilder.durable(QUEUE_FACE_CHECKIN).build();
    }

    @Bean
    public Queue checkOutQueue() {
        return QueueBuilder.durable(QUEUE_FACE_CHECKOUT).build();
    }

    @Bean
    public Binding checkInBinding(Queue checkInQueue, TopicExchange faceEventExchange) {
        return BindingBuilder.bind(checkInQueue)
                .to(faceEventExchange)
                .with(ROUTING_KEY_CHECKIN);
    }

    @Bean
    public Binding checkOutBinding(Queue checkOutQueue, TopicExchange faceEventExchange) {
        return BindingBuilder.bind(checkOutQueue)
                .to(faceEventExchange)
                .with(ROUTING_KEY_CHECKOUT);
    }
}
