package user.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.ServiceException;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import pojo.ResultMsg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class OssAvatarUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.bucket}")
    private String bucket;
    @Value("${aliyun.oss.access-key}")
    private String accessKey;
    @Value("${aliyun.oss.secret-key}")
    private String secretKey;
    @Value("${aliyun.oss.dir-prefix}")
    private String dirPrefix;
    @Value("${aliyun.oss.illegal-dir-prefix}")
    private String illegalDirPrefix;
    @Value("${aliyun.oss.local-base}")
    private String localBase;
    /**
     * 上传 MultipartFile，返回可直接嵌入 <img> 的永久 HTTPS 地址
     */
    public ResultMsg<String> uploadAvatar(MultipartFile file, Integer userId) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        // 拼接路径：avatar/2025/07/1001.jpg
        String objectKey = dirPrefix
                + userId + "." + ext;

        OSS oss = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
        try (InputStream in = file.getInputStream()) {
            oss.putObject(bucket, objectKey, in);
        } catch (Exception e) {
            throw new ServiceException("头像上传失败");
        } finally {
            oss.shutdown();
        }
        // 返回永久外链
        String url="https://" + bucket + "." + endpoint + "/" + objectKey;
        return ResultMsg.success(url,"上传成功");
    }
    public ResultMsg<List<String>> downloadFaceCache() throws Exception {
//        String localBase = "D:\\IdeaProjects\\AttendanceSystem\\src\\main\\resources\\data\\face_cache";
        Path dir = Paths.get(localBase);
        Files.createDirectories(dir);   // 比 mkdirs() 更直观

        List<String> successList = new ArrayList<>();
        OSS oss = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
        try {
            ObjectListing objectListing = oss.listObjects(bucket, dirPrefix);
            for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
                String ossKey = summary.getKey();
                // 跳过目录本身
                if (ossKey.endsWith("/")) continue;

                String fileName = ossKey.substring(ossKey.lastIndexOf('/') + 1);
                if (fileName.isEmpty()) continue;

                Path localFile = dir.resolve(fileName);

                try (OSSObject ossObject = oss.getObject(bucket, ossKey);
                     InputStream in = ossObject.getObjectContent()) {
                    Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
                    successList.add(fileName);
                    log.info("已下载: {} -> {}", ossKey, localFile.toAbsolutePath());
                }
            }
        } finally {
            oss.shutdown();
        }
        return ResultMsg.success(successList,"下载成功");
    }


    public ResultMsg<String> moveFile(String fileName, String sourceDir, String targetDir) {
        // 从完整HTTP路径中提取文件名
        String actualFileName = Paths.get(fileName).getFileName().toString();
        String sourceKey = sourceDir + "/" + actualFileName;
        String targetKey = targetDir + "/" + actualFileName;

        OSS oss = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
        try {
            // 复制文件到目标位置
            oss.copyObject(bucket, sourceKey, bucket, targetKey);

            // 删除原文件
            oss.deleteObject(bucket, sourceKey);

            String targetUrl = "https://" + bucket + "." + endpoint + "/" + targetKey;
            return ResultMsg.success(targetUrl, "文件移动成功");
        } catch (Exception e) {
            log.error("文件移动失败: {}", e.getMessage());
            throw new ServiceException("文件移动失败");
        } finally {
            oss.shutdown();
        }
    }

    /**
     * 便捷方法：将文件从dirPrefix移动到illegaldirprefix
     * @param fileName 文件名
     * @return ResultMsg<String> 操作结果
     */
    public ResultMsg<String> moveFileToIllegal(String fileName) {
        return moveFile(fileName, dirPrefix, illegalDirPrefix);  // 假设illegaldirprefix为"illegal"
    }
}