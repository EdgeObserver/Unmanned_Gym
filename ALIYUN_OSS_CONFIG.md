# 阿里云 OSS 配置指南

## ⚠️ 重要安全提示

**所有配置文件中的阿里云 AccessKey 已替换为占位符，需要您手动配置真实的密钥信息。**

## 📋 需要配置的文件

以下文件中的阿里云 OSS 配置需要填写您的真实 AccessKey：

### 1. FaceService
**文件路径**: `services/FaceService/src/main/resources/application.yml`

```yaml
aliyun:
  oss:
    endpoint:   oss-cn-beijing.aliyuncs.com
    bucket:     Your_ID  # ← 替换为您的 OSS 存储空间名称
    access-key: YOUR_ALIYUN_ACCESS_KEY_ID      # ← 替换为您的 AccessKey ID
    secret-key: YOUR_ALIYUN_ACCESS_KEY_SECRET  # ← 替换为您的 AccessKey Secret
    dir-prefix: user_face/
    illegal_dir-prefix: illegal_user_face/
    local-base: D:\IdeaProjects\AttendanceSystem\data\face_cache
```

### 2. UserService
**文件路径**: `services/UserService/src/main/resources/application.yml`

```yaml
aliyun:
  oss:
    endpoint:   oss-cn-beijing.aliyuncs.com
    bucket:     Your_ID  # ← 替换为您的 OSS 存储空间名称
    access-key: YOUR_ALIYUN_ACCESS_KEY_ID      # ← 替换为您的 AccessKey ID
    secret-key: YOUR_ALIYUN_ACCESS_KEY_SECRET  # ← 替换为您的 AccessKey Secret
    dir-prefix: user_face/
    illegal_dir-prefix: illegal_user_face/
    local-base: D:\IdeaProjects\AttendanceSystem\data\face_cache
```

### 3. ResourceService
**文件路径**: `services/ResourceService/src/main/resources/application.yml`

```yaml
aliyun:
  oss:
    endpoint:   oss-cn-beijing.aliyuncs.com
    bucket:     Your_ID  # ← 替换为您的 OSS 存储空间名称
    access-key: YOUR_ALIYUN_ACCESS_KEY_ID      # ← 替换为您的 AccessKey ID
    secret-key: YOUR_ALIYUN_ACCESS_KEY_SECRET  # ← 替换为您的 AccessKey Secret
    dir-prefix: user_face/
    illegal_dir-prefix: illegal_user_face/
    local-base: D:\IdeaProjects\AttendanceSystem\data\face_cache
```

### 4. RecordService（已注释）
**文件路径**: `services/RecordService/src/main/resources/application.yml`

此服务中的 OSS 配置已被注释，如需启用请取消注释并填写密钥。

### 5. 旧版 AttendanceSystem
**文件路径**: `src/main/resources/application.yml`

```yaml
aliyun:
  oss:
    endpoint:   oss-cn-beijing.aliyuncs.com
    bucket:     edge-observer
    access-key: YOUR_ALIYUN_ACCESS_KEY_ID      # ← 替换为您的 AccessKey ID
    secret-key: YOUR_ALIYUN_ACCESS_KEY_SECRET  # ← 替换为您的 AccessKey Secret
    dir-prefix: user_face/
```

## 🔑 如何获取阿里云 AccessKey

### 步骤 1: 登录阿里云控制台

访问 [阿里云官网](https://www.aliyun.com/) 并登录您的账号。

### 步骤 2: 创建 RAM 用户（推荐）

为了安全起见，建议创建专门的 RAM 子账号，而不是使用主账号的 AccessKey。

1. 进入 [RAM 访问控制](https://ram.console.aliyun.com/)
2. 点击 **用户** > **创建用户**
3. 填写用户名（如：`unmanned-gym-oss`）
4. 勾选 **OpenAPI 调用访问**
5. 点击 **确定**

### 步骤 3: 授予 OSS 权限

1. 在用户列表中找到刚创建的用户
2. 点击 **添加权限**
3. 搜索并选择以下权限策略：
   - `AliyunOSSFullAccess`（完整 OSS 权限）或
   - `AliyunOSSReadOnlyAccess`（只读权限，根据需求选择）
4. 点击 **确定**

### 步骤 4: 获取 AccessKey

1. 在用户详情页，切换到 **认证管理** 标签
2. 点击 **创建 AccessKey**
3. 下载或复制 AccessKey ID 和 AccessKey Secret
4. **⚠️ 重要**: AccessKey Secret 只显示一次，请妥善保存！

## 🛡️ 安全最佳实践

### 1. 使用环境变量（推荐）

不要直接在配置文件中硬编码 AccessKey，而是使用环境变量：

```yaml
aliyun:
  oss:
    access-key: ${ALIYUN_ACCESS_KEY_ID}
    secret-key: ${ALIYUN_ACCESS_KEY_SECRET}
```

然后在系统中设置环境变量：

**Windows (PowerShell)**:
```powershell
$env:ALIYUN_ACCESS_KEY_ID="LTAI5t..."
$env:ALIYUN_ACCESS_KEY_SECRET="BZaJMG..."
```

**Linux/Mac**:
```bash
export ALIYUN_ACCESS_KEY_ID="LTAI5t..."
export ALIYUN_ACCESS_KEY_SECRET="BZaJMG..."
```

### 2. 使用 Nacos 配置中心（生产环境推荐）

将敏感配置存储在 Nacos 配置中心，而不是本地配置文件：

1. 在 Nacos 控制台创建配置文件
2. 将 AccessKey 配置在 Nacos 中
3. 应用启动时从 Nacos 读取配置

**application.yml**:
```yaml
spring:
  config:
    import:
      - nacos:oss-config.properties
```

**Nacos 配置 (oss-config.properties)**:
```properties
aliyun.oss.access-key=LTAI5t...
aliyun.oss.secret-key=BZaJMG...
```

### 3. 使用 STS 临时凭证（最安全）

对于生产环境，建议使用 STS（Security Token Service）临时凭证：

```java
@Service
public class OssService {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    
    @Value("${aliyun.ram.role.arn}")
    private String roleArn;
    
    public OSS createOssClient() {
        // 使用 STS 获取临时凭证
        AssumeRoleResponse response = stsClient.assumeRole(
            new AssumeRoleRequest()
                .setRoleArn(roleArn)
                .setRoleSessionName("oss-session")
        );
        
        return new OSSClientBuilder().build(
            endpoint,
            response.getCredentials().getAccessKeyId(),
            response.getCredentials().getAccessKeySecret(),
            response.getCredentials().getSecurityToken()
        );
    }
}
```

### 4. 配置文件保护

确保敏感配置文件不会被提交到 Git：

**.gitignore**:
```gitignore
# 包含敏感信息的配置文件
**/application-local.yml
**/application-prod.yml
*.secret.yml
.env
```

## 📝 配置验证

配置完成后，可以通过以下方式验证配置是否正确：

### 方法 1: 启动服务检查日志

启动服务后，查看日志中是否有 OSS 相关的错误信息。

### 方法 2: 测试上传功能

```java
@RestController
public class TestController {
    
    @Autowired
    private OssAvatarUtil ossUtil;
    
    @GetMapping("/test/oss")
    public String testOss() {
        try {
            // 尝试列出 Bucket 中的文件
            List<String> files = ossUtil.listAllKeys();
            return "OSS 连接成功，文件数量: " + files.size();
        } catch (Exception e) {
            return "OSS 连接失败: " + e.getMessage();
        }
    }
}
```

### 方法 3: 使用阿里云 CLI 工具

```bash
# 安装阿里云 CLI
pip install aliyun-cli

# 配置 AccessKey
aliyun configure

# 测试 OSS 连接
aliyun oss ls oss://edge-observer
```

## ⚠️ 常见问题

### Q1: 提示 "AccessKey 无效"

**原因**: AccessKey ID 或 Secret 填写错误

**解决**: 
1. 检查是否有空格或特殊字符
2. 确认 AccessKey 状态为"启用"
3. 重新创建 AccessKey

### Q2: 提示 "权限不足"

**原因**: RAM 用户没有 OSS 权限

**解决**:
1. 检查 RAM 用户的权限策略
2. 确保添加了 `AliyunOSSFullAccess` 或相应权限
3. 等待几分钟让权限生效

### Q3: Endpoint 应该填什么？

根据您的 OSS Bucket 所在区域填写：

| 区域 | Endpoint |
|------|----------|
| 华北1（青岛） | oss-cn-qingdao.aliyuncs.com |
| 华北2（北京） | oss-cn-beijing.aliyuncs.com |
| 华东1（杭州） | oss-cn-hangzhou.aliyuncs.com |
| 华东2（上海） | oss-cn-shanghai.aliyuncs.com |
| 华南1（深圳） | oss-cn-shenzhen.aliyuncs.com |

查看您的 Bucket 所在区域：[OSS 控制台](https://oss.console.aliyun.com/bucket)

### Q4: 如何轮换 AccessKey？

1. 创建新的 AccessKey
2. 更新应用配置
3. 重启应用
4. 验证新 Key 正常工作
5. 禁用并删除旧的 AccessKey

## 🔗 相关资源

- [阿里云 RAM 文档](https://help.aliyun.com/product/28625.html)
- [OSS SDK 文档](https://help.aliyun.com/product/31815.html)
- [STS 临时凭证](https://help.aliyun.com/product/28672.html)
- [安全最佳实践](https://help.aliyun.com/document_detail/102606.html)

## 📞 技术支持

如有问题，请：
1. 查阅阿里云官方文档
2. 提交 GitHub Issue
3. 联系项目维护团队

---

**最后更新**: 2024-01-15  
**版本**: 1.0
