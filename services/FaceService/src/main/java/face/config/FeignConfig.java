package face.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                10000,  // 连接超时 10 秒
                30000   // 读超时 30 秒
        );
    }
}
