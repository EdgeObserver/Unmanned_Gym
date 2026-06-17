package count.serivce.impl;

import count.serivce.CountService;
import count.util.PersonCountHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pojo.ResultMsg;

import java.io.IOException;
import java.util.Map;

import static java.lang.Math.max;

@Service
@RequiredArgsConstructor
public class CountServiceImpl implements CountService {
    @Autowired
    private PersonCountHolder personCountHolder;
    private final SimpMessagingTemplate ws;
    private final java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
    @Value("${count.port:8000}")
    private int port;
    private String lastPushColor="";

    @Override
    public ResultMsg<Integer> getCurrentCount() {
        int curCount = personCountHolder.getCurrent();
        return ResultMsg.success(curCount, "获取成功");
    }

    @Override
    public ResultMsg<String> leave() {
        personCountHolder.setCurrent(max(0,personCountHolder.getCurrent()-1));
        return ResultMsg.success("","离开成功");
    }

    @Override
    public ResultMsg<String> arrive() {
        personCountHolder.setCurrent(personCountHolder.getCurrent()+1);
        return ResultMsg.success("","到达成功");
    }

    @Override
    public ResultMsg<Integer> checkCount(int fresh) {
        ResultMsg<Integer> result = personCountHolder.checkCount(fresh);
        if(result.getCode()==200){
            return ResultMsg.success(result.getData(),result.getMsg());
        }else{
            return ResultMsg.fail(result.getData(),result.getMsg());
        }
    }

    @Override
    @Scheduled(fixedDelay = 500)
    public void pushColor() {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:" + port + "/py/person_count"))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(response.body());
            int fresh = rootNode.get("person_count").asInt();
            ResultMsg<Integer> result = checkCount(fresh);
            String color = result.getCode() == 500 ? "red" : "green";
//            System.out.println("[WebSocket] 检测结果: " + result.getMsg() + ", 颜色: " + color);
            if (!color.equals(lastPushColor)) {      // 变化才推
                lastPushColor = color;

                // ① 全局广播 /topic/color
                ws.convertAndSend("/topic/color", Map.of("color", color));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
