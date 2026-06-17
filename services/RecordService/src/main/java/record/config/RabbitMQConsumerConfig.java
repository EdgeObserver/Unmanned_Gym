package record.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {

    /**
     * 配置ObjectMapper用于手动JSON解析
     * 注意：这里只定义ObjectMapper，不定义MessageConverter和RabbitTemplate
     * 因为我们的监听器使用手动解析方式（接收Message对象）
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
