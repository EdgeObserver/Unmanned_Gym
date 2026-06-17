package knowledge.service.impl;

import knowledge.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pojo.ResultMsg;

import java.io.IOException;

@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    @Value("${knowledge.port:7050}")
    private int port;
    @Override
    public ResultMsg getKnowledge(String question) throws IOException, InterruptedException {
        return ResultMsg.success(sendQuestion(question),"获取回答成功");
    }
    private String sendQuestion(String question) throws IOException, InterruptedException {
        // 构建JSON请求体
        String jsonBody = "{\"question\":\"" + question + "\"}";

        // 发送HTTP POST请求到Python服务
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:" + port + "/ask"))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(java.time.Duration.ofSeconds(30))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofString());

        // 解析JSON响应并获取answer字段
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(response.body());
        String answer = rootNode.get("answer").asText();
        System.out.println("知识问答响应: " + response.body());
        return answer;
    }
}
