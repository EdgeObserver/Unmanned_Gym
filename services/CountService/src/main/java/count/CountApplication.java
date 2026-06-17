package count;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling  // 启用定时任务
@SpringBootApplication(scanBasePackages = {"count"})
public class CountApplication {
    public static void main(String[]args){
        SpringApplication.run(CountApplication.class,args);
    }
}
