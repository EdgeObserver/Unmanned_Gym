package count.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pojo.ResultMsg;

import java.io.IOException;
import java.util.List;

@Component
public class CountServiceInitializer {
    @Value("${count.python.script-path:../python/CountService.py}")
    private String pythonScriptPath;
    @Value("${count.port:8000}")
    private int port;
    private Process pythonProcess;
    @PostConstruct
    public void initialize() {
        try {
            System.out.println("正在启动人数统计Python服务...");

            ProcessBuilder pb = new ProcessBuilder("python", "-u",pythonScriptPath);
            pb.directory(new java.io.File(System.getProperty("user.dir")).getParentFile());
            pb.redirectErrorStream(true);
            pythonProcess = pb.start();

            startPythonOutputReader(pythonProcess);
            waitForPythonServiceReady();

            System.out.println("人数统计服务已就绪，摄像头自动启动中...");

        } catch (Exception e) {
            System.err.println("人数统计服务初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
    
    @PreDestroy
    public void cleanup() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
            try {
                pythonProcess.waitFor();
                System.out.println("Python进程已停止");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("等待Python进程结束时被中断: " + e.getMessage());
            }
        }
    }
}
