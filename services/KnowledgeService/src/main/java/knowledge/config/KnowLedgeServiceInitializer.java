package knowledge.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import pojo.ResultMsg;

import java.util.List;
@Component
public class KnowLedgeServiceInitializer {
    @Value("${knowledge.script-path:../python/Service.py}")
    private String pythonScriptPath;
    @Value("${knowledge.port:7050}")
    private int port;

    private Process pythonProcess;
    /**
     * 服务启动时初始化
     */
    @PostConstruct
    public void initialize() {
        try {
            System.out.println("正在启动知识库Python服务...");
            ProcessBuilder pb = new ProcessBuilder("python", "-u",pythonScriptPath);
//            pb.directory(new java.io.File(System.getProperty("user.dir")).getParentFile());
            pb.directory(new java.io.File(System.getProperty("user.dir")).getParentFile());
            pb.redirectErrorStream(true); // 将错误流合并到输出流
            pythonProcess = pb.start();
//            startPythonProcessMonitor();
            // 等待Python服务就绪

        } catch (Exception e) {
            System.err.println("知识库Python服务初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
