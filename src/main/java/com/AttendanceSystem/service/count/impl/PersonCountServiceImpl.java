package com.AttendanceSystem.service.count.impl;
import com.AttendanceSystem.pojo.ResultMsg;
import com.AttendanceSystem.service.count.PersonCountService;

import com.AttendanceSystem.util.PersonCountHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonCountServiceImpl implements PersonCountService {


    private final SupervisionServiceImpl SSI;
    private final PersonCountHolder PCH;
    private final ColorPushService CPS;
    private String lastPushColor="";
    @Override
    @Scheduled(fixedDelay = 500)
    public void poll() {
        int fresh = SSI.getPersonCount();
        ResultMsg<Integer> result = PCH.arrive(fresh);
        String color = result.getCode() == 500 ? "red" : "green";
//        System.out.println(color);
        if (!color.equals(lastPushColor)) {      // 变化才推
            lastPushColor = color;
            CPS.pushColor(null, color.equals("red"));
        }
    }
}