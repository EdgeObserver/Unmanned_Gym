package com.AttendanceSystem.service.count.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ColorPushService {
    private final SimpMessagingTemplate ws;

    /** 0=绿色 1=红色 */
    public void pushColor(Integer userId, boolean red){
        String color = red ? "red" : "green";
        // ① 全局广播 /topic/color
        ws.convertAndSend("/topic/color", Map.of("color",color));
        // ② 若只想给当前用户推，用 /user/{userId}/topic/color 下文用全局即可
    }
}