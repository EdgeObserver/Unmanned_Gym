package com.AttendanceSystem.service.count.impl;



import com.AttendanceSystem.service.count.SupervisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class SupervisionServiceImpl {
    @Autowired
    private SupervisionService ss;
    public int getPersonCount(){
        Map<String,Integer> result=ss.getPersonCount();
        return result.get("person_count");
    }
}
