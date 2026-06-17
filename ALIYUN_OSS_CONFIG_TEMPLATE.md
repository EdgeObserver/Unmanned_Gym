# 阿里云 OSS 配置模板

## 📋 配置说明

本文档提供阿里云 OSS 配置的完整模板，所有敏感信息已使用占位符表示。

## 🔧 配置文件模板

### application.yml 配置示例

```yaml
aliyun:
  oss:
    # OSS Endpoint（根据您的 Bucket 所在区域选择）
    # 华北2（北京）: oss-cn-beijing.aliyuncs.com
    # 华东1（杭州）: oss-cn-hangzhou.aliyuncs.com
    # 华东2（上海）: oss-cn-shanghai.aliyuncs.com
    # 华南1（深圳）: oss-cn-shenzhen.aliyuncs.com
    endpoint:   YOUR_OSS_ENDPOINT
    
    # OSS Bucket 名称
    bucket:     YOUR_OSS_BUCKET_NAME
    
    # AccessKey ID（从阿里云 RAM 控制台获取）
    access-key: YOUR_ALIYUN_ACCESS_KEY_ID
    
    # AccessKey Secret（从阿里云 RAM 控制台获取）
    secret-key: YOUR_ALIYUN_ACCESS_KEY_SECRET
    
    # 文件存储目录前缀
    dir-prefix: user_face/              # 用户头像目录
    illegal_dir-prefix: illegal_user_face/  # 违规图片目录
    
    # 本地缓存路径（可选）
    local-base: /path/to/local/cache
```

### 环境变量方式（推荐）

```yaml
aliyun:
  oss:
    endpoint:   ${OSS_ENDPOINT:oss-cn-beijing.aliyuncs.com}
    bucket:     ${OSS_BUCKET_NAME}
    access-key: ${ALIYUN_ACCESS_KEY_ID}
    secret-key: ${ALIYUN_ACCESS_KEY_SECRET}
    dir-prefix: ${OSS_DIR_PREFIX:user_face/}
    illegal_dir-prefix: ${OSS_ILLEGAL_DIR_PREFIX:illegal_user_face/}
    local-base: ${OSS_LOCAL_BASE:/tmp/oss-cache}
```

设置环境变量：

**Windows (PowerShell)**:
```powershell
$env:OSS_ENDPOINT="oss-cn-beijing.aliyuncs.com"
$env:OSS_BUCKET_NAME="your-bucket-name"
$env:ALIYUN_ACCESS_KEY_ID="LTAI5t..."
$env:ALIYUN_ACCESS_KEY_SECRET="your-secret-key"
```

**Linux/Mac**:
```bash
export OSS_ENDPOINT="oss-cn-beijing.aliyuncs.com"
export OSS_BUCKET_NAME="your-bucket-name"
export ALIYUN_ACCESS_KEY_ID="LTAI5t..."
export ALIYUN_ACCESS_KEY_SECRET="your-secret-key"
```

### .env 文件方式

创建 `.env` 文件：

```env
# OSS Configuration
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET_NAME=your-bucket-name
ALIYUN_ACCESS_KEY_ID=LTAI5t...
ALIYUN_ACCESS_KEY_SECRET=your-secret-key
OSS_DIR_PREFIX=user_face/
OSS_ILLEGAL_DIR_PREFIX=illegal_user_face/
OSS_LOCAL_BASE=/tmp/oss-cache
```

确保 `.env` 在 `.gitignore` 中：

```gitignore
.env
.env.local
.env.*.local
```

## 🔑 如何获取配置信息

### 1. 获取 OSS Endpoint

登录 [阿里云 OSS 控制台](https://oss.console.aliyun.com/bucket)，查看您的 Bucket 所在区域对应的 Endpoint：

| 区域 | Endpoint |
|------|----------|
| 华北1（青岛） | oss-cn-qingdao.aliyuncs.com |
| 华北2（北京） | oss-cn-beijing.aliyuncs.com |
| 华东1（杭州） | oss-cn-hangzhou.aliyuncs.com |
| 华东2（上海） | oss-cn-shanghai.aliyuncs.com |
| 华南1（深圳） | oss-cn-shenzhen.aliyuncs.com |
| 西南1（成都） | oss-cn-chengdu.aliyuncs.com |

### 2. 获取 Bucket 名称

在 OSS 控制台创建 Bucket 后，Bucket 名称会显示在列表中。

**命名规则**:
- 只能包含小写字母、数字和短横线（-）
- 必须以小写字母或数字开头和结尾
- 长度必须在 3-63 字符之间

### 3. 获取 AccessKey

#### 方式一：使用主账号 AccessKey（不推荐）

1. 登录 [阿里云控制台](https://www.aliyun.com/)
2. 鼠标悬停在右上角头像，点击 **AccessKey 管理**
3. 创建或查看 AccessKey

⚠️ **警告**: 主账号 AccessKey 拥有所有权限，存在安全风险！

#### 方式二：使用 RAM 子账号 AccessKey（推荐）

1. 进入 [RAM 访问控制](https://ram.console.aliyun.com/)
2. 创建 RAM 用户
3. 授予最小权限（如 `AliyunOSSFullAccess`）
4. 为该用户创建 AccessKey

## 🛡️ 安全最佳实践

### 1. 最小权限原则

为应用创建专用的 RAM 用户，仅授予必要的权限：

```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "oss:GetObject",
        "oss:PutObject",
        "oss:DeleteObject",
        "oss:ListObjects"
      ],
      "Resource": [
        "acs:oss:*:*:your-bucket-name/*"
      ]
    }
  ]
}
```

### 2. 使用 STS 临时凭证（生产环境推荐）

```java
@Service
public class OssService {
    
    @Value("${aliyun.ram.role.arn}")
    private String roleArn;
    
    public OSS createOssClient() {
        // 使用 STS 获取临时凭证
        AssumeRoleResponse response = stsClient.assumeRole(
            new AssumeRoleRequest()
                .setRoleArn(roleArn)
                .setRoleSessionName("oss-session")
                .setDurationSeconds(3600)  // 1小时有效期
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

### 3. 定期轮换密钥

- 建议每 90 天轮换一次 AccessKey
- 启用操作审计监控异常访问
- 设置告警规则检测异常行为

### 4. 配置加密

使用 Nacos 配置中心时，启用配置加密：

```properties
# Nacos 配置
spring.cloud.nacos.config.encrypt.enabled=true
```

## ✅ 配置验证

### 方法 1: 使用阿里云 CLI

```bash
# 安装阿里云 CLI
pip install aliyun-cli

# 配置 AccessKey
aliyun configure

# 测试 OSS 连接
aliyun oss ls oss://YOUR_OSS_BUCKET_NAME
```

### 方法 2: Java 代码测试

```java
@RestController
public class OssTestController {
    
    @Autowired
    private OssAvatarUtil ossUtil;
    
    @GetMapping("/test/oss")
    public ResponseEntity<?> testOss() {
        try {
            // 测试列出文件
            List<String> files = ossUtil.listAllKeys();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "OSS 连接成功");
            result.put("fileCount", files.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "OSS 连接失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }
}
```

### 方法 3: 启动日志检查

启动应用后，查看日志中是否有以下错误：
- `AccessKey ID does not exist`
- `The Access Key Id you provided does not exist in our records`
- `SignatureDoesNotMatch`

## ❓ 常见问题

### Q1: 提示 "Endpoint 格式错误"

**原因**: Endpoint 格式不正确

**解决**: 
- 确保格式为 `oss-{region}.aliyuncs.com`
- 不要包含 `http://` 或 `https://` 前缀

### Q2: 提示 "Bucket 不存在"

**原因**: Bucket 名称错误或 Bucket 不在该区域

**解决**:
- 检查 Bucket 名称是否正确
- 确认 Endpoint 与 Bucket 所在区域匹配

### Q3: 提示 "权限不足"

**原因**: RAM 用户没有 OSS 权限

**解决**:
- 检查 RAM 用户的权限策略
- 确保添加了 `AliyunOSSFullAccess` 或自定义权限

### Q4: 如何切换 Bucket 区域？

修改 `endpoint` 为对应区域的地址：

```yaml
# 从北京切换到杭州
endpoint: oss-cn-hangzhou.aliyuncs.com  # 原来是 oss-cn-beijing.aliyuncs.com
```

## 📚 相关资源

- [阿里云 OSS 官方文档](https://help.aliyun.com/product/31815.html)
- [RAM 访问控制](https://help.aliyun.com/product/28625.html)
- [STS 临时凭证](https://help.aliyun.com/product/28672.html)
- [OSS SDK for Java](https://help.aliyun.com/document_detail/32008.html)
- [安全最佳实践](https://help.aliyun.com/document_detail/102606.html)

## 🔗 项目相关文档

- [SECURITY_REPORT.md](SECURITY_REPORT.md) - 安全处理报告
- [CONFIG_PASSWORD_REPLACEMENT_GUIDE.md](CONFIG_PASSWORD_REPLACEMENT_GUIDE.md) - 密码配置指南
- [check_secrets.ps1](check_secrets.ps1) - 安全检查脚本

---

**最后更新**: 2024-01-15  
**版本**: 2.0  
**状态**: ✅ 所有敏感信息已使用占位符
