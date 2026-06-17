# 🔐 安全配置指南

> 本文档整合了所有安全相关的配置信息，包括阿里云 OSS、敏感信息管理、密钥轮换等。

## 📋 目录

- [敏感信息管理](#敏感信息管理)
- [阿里云 OSS 配置](#阿里云-oss-配置)
- [数据库密码配置](#数据库密码配置)
- [安全检查工具](#安全检查工具)
- [安全最佳实践](#安全最佳实践)

---

## 🔑 敏感信息管理

### 当前状态

✅ **已完成**：所有敏感信息已迁移至 `.env` 文件管理

**配置文件位置**：
- [.env.example](.env.example) - 配置模板（可提交到 Git）
- [.env](.env) - 真实配置（已在 .gitignore 中，不提交）

### 敏感信息类型

| 类型 | 示例 | 存储位置 | 状态 |
|------|------|---------|------|
| AccessKey ID | LTAI5t... | .env | ✅ 已保护 |
| AccessKey Secret | BZaJMG... | .env | ✅ 已保护 |
| 数据库密码 | your_password | .env | ✅ 已保护 |
| Redis 密码 | your_redis_pass | .env | ✅ 已保护 |
| RabbitMQ 密码 | your_rabbitmq_pass | .env | ✅ 已保护 |

### 配置文件示例

**application.yml**（使用环境变量）：
```yaml
aliyun:
  oss:
    endpoint:   ${OSS_ENDPOINT}
    bucket:     ${OSS_BUCKET_NAME}
    access-key: ${ALIYUN_ACCESS_KEY_ID}
    secret-key: ${ALIYUN_ACCESS_KEY_SECRET}

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:unmanned_gym}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
```

**.env**（真实配置）：
```env
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET_NAME=your-bucket-name
ALIYUN_ACCESS_KEY_ID=LTAI5t...
ALIYUN_ACCESS_KEY_SECRET=your-secret-key
DB_PASSWORD=your_mysql_password
```

---

## ☁️ 阿里云 OSS 配置

### 配置步骤

#### 1. 获取阿里云 AccessKey

**推荐方式**：使用 RAM 子账号

1. 登录 [阿里云 RAM 控制台](https://ram.console.aliyun.com/)
2. 创建 RAM 用户（如：`unmanned-gym-oss`）
3. 授予权限：`AliyunOSSFullAccess`
4. 创建 AccessKey

⚠️ **重要**：不要使用主账号的 AccessKey！

#### 2. 配置环境变量

编辑 `.env` 文件：

```env
# OSS Endpoint（根据 Bucket 所在区域选择）
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com

# Bucket 名称
OSS_BUCKET_NAME=your-bucket-name

# AccessKey
ALIYUN_ACCESS_KEY_ID=LTAI5t...
ALIYUN_ACCESS_KEY_SECRET=your-secret-key

# 文件存储路径
OSS_DIR_PREFIX=user_face/
OSS_ILLEGAL_DIR_PREFIX=illegal_user_face/
OSS_LOCAL_BASE=D:/data/face_cache
```

#### 3. 验证配置

运行验证脚本：

```powershell
.\verify_env.ps1
```

预期输出：
```
✅ 已配置: ALIYUN_ACCESS_KEY_ID
✅ 已配置: ALIYUN_ACCESS_KEY_SECRET
✅ 已配置: OSS_ENDPOINT
✅ 已配置: OSS_BUCKET_NAME
```

### OSS Endpoint 对照表

| 区域 | Endpoint |
|------|----------|
| 华北1（青岛） | oss-cn-qingdao.aliyuncs.com |
| 华北2（北京） | oss-cn-beijing.aliyuncs.com |
| 华东1（杭州） | oss-cn-hangzhou.aliyuncs.com |
| 华东2（上海） | oss-cn-shanghai.aliyuncs.com |
| 华南1（深圳） | oss-cn-shenzhen.aliyuncs.com |

查看您的 Bucket 区域：[OSS 控制台](https://oss.console.aliyun.com/bucket)

---

## 💾 数据库密码配置

### MySQL 密码配置

在 `.env` 文件中配置：

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=unmanned_gym
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
```

### Redis 密码配置

```env
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

### RabbitMQ 密码配置

```env
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=your_rabbitmq_password
RABBITMQ_VIRTUAL_HOST=/
```

---

## 🔍 安全检查工具

### 1. 验证 .env 配置

```powershell
.\verify_env.ps1
```

**功能**：
- ✅ 检查 .env 文件是否存在
- ✅ 验证必需的配置项
- ✅ 检测是否仍使用占位符
- ✅ 确认 .env 在 .gitignore 中

### 2. 扫描敏感信息泄露

```powershell
.\check_secrets.ps1
```

**检测内容**：
- 🔍 真实的 AccessKey ID（LTAI 开头）
- 🔍 硬编码的密码
- 🔍 Secret Key
- 🔍 API Key
- 🔍 JWT Token
- 🔍 私钥文件

### 3. 验证阿里云信息清理

```powershell
.\verify_aliyun_cleanup.ps1
```

**检查项**：
- ✅ 无真实的 AccessKey ID
- ✅ 无真实的 OSS Endpoint
- ✅ 无真实的 Bucket 名称
- ✅ 无长字符串（可能是 Secret）

---

## 🛡️ 安全最佳实践

### 1. .env 文件保护

✅ **已在 .gitignore 中**：
```gitignore
.env
.env.local
.env.*.local
*.secret.yml
```

❌ **切勿提交**：
- 永远不要将 `.env` 提交到 Git
- 使用 `.env.example` 作为模板
- 每个开发者独立配置

### 2. 密钥轮换策略

**建议周期**：
- AccessKey：每 90 天
- 数据库密码：每 90 天
- JWT Secret：每 180 天

**轮换步骤**：
1. 创建新的密钥
2. 更新 `.env` 文件
3. 重启应用
4. 验证新密钥正常工作
5. 禁用旧密钥

### 3. 最小权限原则

**RAM 用户权限**：
```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "oss:GetObject",
        "oss:PutObject",
        "oss:DeleteObject"
      ],
      "Resource": [
        "acs:oss:*:*:your-bucket/*"
      ]
    }
  ]
}
```

**数据库权限**：
- 仅授予必要的 CRUD 权限
- 不要使用 root 账号
- 限制访问 IP

### 4. 启用操作审计

**阿里云 ActionTrail**：
1. 登录 [ActionTrail 控制台](https://actiontrail.console.aliyun.com/)
2. 启用操作审计
3. 设置告警规则
4. 定期审查日志

**监控项**：
- 异常的 OSS 访问
- 频繁的认证失败
- 非工作时间的操作

### 5. 生产环境建议

**使用 Nacos 配置中心**：

1. 在 Nacos 创建配置文件
2. 启用配置加密
3. 设置访问权限
4. 应用启动时自动读取

**application.yml**：
```yaml
spring:
  config:
    import:
      - nacos:oss-config.properties
```

**Nacos 配置**（加密）：
```properties
aliyun.oss.access-key=ENC(encrypted_value)
aliyun.oss.secret-key=ENC(encrypted_value)
```

---

## ⚠️ 应急响应

### 发现密钥泄露怎么办？

1. **立即禁用**泄露的 AccessKey
   - 登录 RAM 控制台
   - 找到对应的 AccessKey
   - 点击"禁用"

2. **评估影响范围**
   - 检查操作审计日志
   - 确认是否有异常访问
   - 评估数据泄露风险

3. **轮换所有相关凭证**
   - 创建新的 AccessKey
   - 更新数据库密码
   - 更换 Redis 密码

4. **审计访问日志**
   - 下载 OSS 访问日志
   - 分析异常行为
   - 报告安全事件

5. **更新安全措施**
   - 加强权限控制
   - 启用 MFA
   - 增加监控频率

---

## 📚 相关文档

- [README.md](README.md) - 项目主文档
- [ENV_SETUP_GUIDE.md](ENV_SETUP_GUIDE.md) - 环境变量配置指南
- [DATABASE_DESIGN.md](DATABASE_DESIGN.md) - 数据库设计文档
- [.env.example](.env.example) - 环境变量模板

---

## 🔗 参考资源

- [阿里云 RAM 文档](https://help.aliyun.com/product/28625.html)
- [OSS 安全最佳实践](https://help.aliyun.com/document_detail/102606.html)
- [STS 临时凭证](https://help.aliyun.com/product/28672.html)
- [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

---

**最后更新**: 2024-01-15  
**版本**: 1.0  
**维护者**: Unmanned Gym Team
