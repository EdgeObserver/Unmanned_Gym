package com.AttendanceSystem.service.count;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient(name = "python-person-start", url = "http://localhost:8000")
public interface SupervisionService {
    @PostMapping("/py/person_count")
    Map<String,Integer> getPersonCount();
}
