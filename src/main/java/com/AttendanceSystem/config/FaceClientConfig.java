package com.AttendanceSystem.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FaceClientConfig {
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
                10000,  // 连接超时 10 秒
                300000  // 读超时 5 分钟（建库可能很慢）
        );
    }
}