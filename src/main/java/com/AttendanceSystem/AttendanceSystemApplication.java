package com.AttendanceSystem;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.AttendanceSystem.service.start_up.WaitPort;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.AttendanceSystem.mapper")
@EnableFeignClients
@EnableScheduling
public class AttendanceSystemApplication {
    public static void main(String[] args) throws InterruptedException {
        WaitPort.waitPorts(5000,5050,8000);
        SpringApplication.run(AttendanceSystemApplication.class,args);
    }

}
