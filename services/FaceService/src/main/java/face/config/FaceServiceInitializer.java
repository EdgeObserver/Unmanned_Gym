package face.config;

import face.utils.OssAvatarUtil;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

import jakarta.annotation.PostConstruct;
import pojo.ResultMsg;

@Component
public class FaceServiceInitializer {
    
    @Value("${face.python.script-path:../python/FaceService.py}")
    private String pythonScriptPath;
    @Value("${face.port:5000}")
    private int port;
    @Autowired
    private OssAvatarUtil ossAvatarUtil;
    private Process pythonProcess;
    /**
     * 服务启动时初始化
     */
    @PostConstruct
    public void initialize() {
        try {
            System.out.println("正在下载人脸缓存...");
            ResultMsg<List<String>> res=ossAvatarUtil.downloadFaceCache();
            System.out.println(res.getMsg());
            System.out.println("正在启动人脸识别Python服务...");

            ProcessBuilder pb = new ProcessBuilder("python", "-u",pythonScriptPath);
//            pb.directory(new java.io.File(System.getProperty("user.dir")).getParentFile());
            pb.directory(new java.io.File(System.getProperty("user.dir")).getParentFile());
            pb.redirectErrorStream(true); // 将错误流合并到输出流
            pythonProcess = pb.start();
//            startPythonProcessMonitor();
            // 等待Python服务就绪
            startPythonOutputReader(pythonProcess);
            waitForPythonServiceReady();

            // 构建人脸数据库
            buildFaceDatabase();
            startFaceRecognition();

        } catch (Exception e) {
            System.err.println("人脸识别服务初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 构建人脸数据库
     */
    private void buildFaceDatabase() throws IOException, InterruptedException {
        // 发送HTTP请求到Python服务构建人脸库
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:" + port + "/py/face/build"))
                .POST(java.net.http.HttpRequest.BodyPublishers.noBody())
                .timeout(java.time.Duration.ofSeconds(30))
                .build();
                
        java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
        // 解析JSON响应并获取data中的nums
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(response.body());
        int nums = rootNode.get("data").get("nums").asInt();
        System.out.println("识别到的人脸数量: " + nums);
        System.out.println("人脸库构建响应: " + response.body());
    }
    /**
     * 启动人脸识别服务
     */
    public void startFaceRecognition() {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("http://localhost:" + port + "/py/face/start"))
                    .POST(java.net.http.HttpRequest.BodyPublishers.noBody())
                    .timeout(java.time.Duration.ofSeconds(30))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            System.out.println("启动人脸识别服务响应: " + response.body());
            


            if (response.statusCode() == 200) {
                System.out.println("人脸识别服务启动成功");
            } else {
                System.err.println("人脸识别服务启动失败");
            }
        } catch (Exception e) {
            System.err.println("启动人脸识别服务时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * 停止人脸识别服务
     */

    private void waitForPythonServiceReady() throws InterruptedException {
        int maxAttempts = 30; // 最大等待时间30秒
        int attempt = 0;

        while (attempt < maxAttempts) {
            if (isPortAvailable(port)) {
                System.out.println("Python服务端口已就绪");
                return;
            }
            System.out.println("等待Python服务启动... (" + (attempt + 1) + "/" + maxAttempts + ")");
            Thread.sleep(1000);
            attempt++;
        }

        throw new RuntimeException("Python服务启动超时");
    }

    private boolean isPortAvailable(int port) {
        try {
            java.net.Socket socket = new java.net.Socket("localhost", port);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    @PreDestroy
    public void cleanup() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
            try {
                pythonProcess.waitFor(); // 等待进程结束
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("等待Python进程结束时被中断: " + e.getMessage());
            }
        }
    }

    private void startPythonOutputReader(Process process) {
        Thread outputThread = new Thread(() -> {
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Python 输出: " + line);
                }
            } catch (Exception e) {
                System.err.println("读取Python输出时出错: " + e.getMessage());
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();
    }
    // 在初始化时启动监控线程
//    private void startPythonProcessMonitor() {
//        Thread monitor = new Thread(() -> {
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                if (pythonProcess != null && pythonProcess.isAlive()) {
//                    pythonProcess.destroy();
//                }
//            }));
//        });
//        monitor.setDaemon(true);
//        monitor.start();
//    }






}
