package user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient//服务发现功能
@SpringBootApplication
public class UserApplication {
    public static void main(String[]args){
        SpringApplication.run(UserApplication.class,args);
    }
}