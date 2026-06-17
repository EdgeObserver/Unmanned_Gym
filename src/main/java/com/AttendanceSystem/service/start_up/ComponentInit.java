package com.AttendanceSystem.service.start_up;

import com.AttendanceSystem.pojo.ResultMsg;
import com.AttendanceSystem.service.face.FaceDataService;
import com.AttendanceSystem.service.face.FacevisionService;
import com.AttendanceSystem.util.OssAvatarUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// infrastructure/startup/FaceBankInitializer.java


@Slf4j
@Component
@Lazy   // ← 延迟到第一次调用时才初始化 FeignClient（避免启动时调）
public class ComponentInit implements ApplicationRunner {

    @Autowired
    private FacevisionService frs;
    @Autowired
    private FaceDataService fds;
    @Autowired
    private OssAvatarUtil oau;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 重试 3 次，间隔 5 秒
        for (int i = 1; true; i++) {
            try {
                ResultMsg<List<String>> result=oau.downloadFaceCache();
                System.out.println("下载人脸信息："+result.getData().size());
                //    @Value("${python.face.data_dir:D:/data}")
                //    private String dbDir;
                //    @Value("${python.face.svPath}")
                //    private String svPath;
                Map<String, String> build_result = fds.Build();
                System.out.println("构建人脸数据库:"+ build_result.get("msg"));
                Map<String,String> rec_result=frs.recognize();
                System.out.println("初始化人脸摄像头:"+rec_result.get("msg"));

                return;
            } catch (Exception e) {
                if (i == 3) throw e;          // 第 3 次仍失败则抛出
                Thread.sleep(5000);           // 5 秒
            }
        }


    }
}
//public class FaceBankInitializer implements ApplicationRunner {
//
//    @Autowired
//    private BuildFaceDataService BFDS;
//
//    @Value("${face.data_dir:D:/data}")
//    private String dbDir;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//
//        // 幂等锁：只有 leader 节点执行
//        if (!acquireLeaderLock()) {
//            log.info("非 Leader 节点，跳过建库");
//            return;
//        }
//        log.info("开始调用 Python 服务建库...");
//        try {
//            Map<String, String> result = BFDS.build(Map.of(
//                    "dbDir", dbDir,
//                    "output", System.getProperty("java.io.tmpdir") + "/face/bank.pk"
//            ));
////            String pkPath = result.get("pkPath");
////            System.setProperty("bank.pk", pkPath);  // 存入环境，供其他 Service 使用
//            log.info("人脸库构建成功，路径：{}", "test");
//        } catch (Exception e) {
//            log.error("建库失败", e);
//            // 启动时失败，可以选择阻止应用继续启动
//            throw new IllegalStateException("人脸库构建失败", e);
//        }
//    }
//
//    private boolean acquireLeaderLock() {
//        // 简单实现：用 Redis SETNX 抢锁
//        // return redisTemplate.opsForValue().setIfAbsent("face:build:lock", "1", 10, TimeUnit.MINUTES);
//        return true;  // 单机场景直接返回 true
//    }
//}