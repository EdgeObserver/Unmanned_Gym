# 🔐 环境变量配置指南

## 📋 概述

本项目已迁移至使用环境变量管理所有敏感配置信息。本指南将帮助您正确配置和使用环境变量。

## ⚠️ 重要安全提示

1. **`.env` 文件包含敏感信息，切勿提交到 Git**
2. `.env` 已在 `.gitignore` 中，确保不会被版本控制
3. 每个开发者需要创建自己的 `.env` 文件
4. 生产环境应使用配置中心（如 Nacos）管理配置

## 🚀 快速开始

### 步骤 1: 创建 .env 文件

```bash
# 复制模板文件
cp .env.example .env

# Windows PowerShell
Copy-Item .env.example .env
```

### 步骤 2: 编辑 .env 文件

打开 `.env` 文件，填写真实的配置信息：

```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=unmanned_gym
DB_USERNAME=root
DB_PASSWORD=your_real_mysql_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# RabbitMQ 配置
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=your_rabbitmq_password

# 阿里云 OSS 配置
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET_NAME=your-bucket-name
ALIYUN_ACCESS_KEY_ID=LTAI5t...
ALIYUN_ACCESS_KEY_SECRET=your-secret-key
```

### 步骤 3: 启动应用

Spring Boot 会自动读取 `.env` 文件中的环境变量。

```bash
# Maven 启动
mvn spring-boot:run

# 或者使用 IDE 直接运行
```

## 📝 配置项说明

### 数据库配置 (MySQL)

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `DB_HOST` | 数据库主机地址 | localhost | ✅ |
| `DB_PORT` | 数据库端口 | 3306 | ✅ |
| `DB_NAME` | 数据库名称 | unmanned_gym | ✅ |
| `DB_USERNAME` | 数据库用户名 | root | ✅ |
| `DB_PASSWORD` | 数据库密码 | - | ✅ |

### Redis 配置

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `REDIS_HOST` | Redis 主机地址 | localhost | ✅ |
| `REDIS_PORT` | Redis 端口 | 6379 | ✅ |
| `REDIS_PASSWORD` | Redis 密码 | - | ❌ |

### RabbitMQ 配置

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `RABBITMQ_HOST` | RabbitMQ 主机地址 | localhost | ✅ |
| `RABBITMQ_PORT` | RabbitMQ 端口 | 5672 | ✅ |
| `RABBITMQ_USERNAME` | RabbitMQ 用户名 | guest | ✅ |
| `RABBITMQ_PASSWORD` | RabbitMQ 密码 | - | ✅ |
| `RABBITMQ_VIRTUAL_HOST` | 虚拟主机 | / | ❌ |

### 阿里云 OSS 配置

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `OSS_ENDPOINT` | OSS Endpoint | - | ✅ |
| `OSS_BUCKET_NAME` | Bucket 名称 | - | ✅ |
| `ALIYUN_ACCESS_KEY_ID` | AccessKey ID | - | ✅ |
| `ALIYUN_ACCESS_KEY_SECRET` | AccessKey Secret | - | ✅ |
| `OSS_DIR_PREFIX` | 文件目录前缀 | user_face/ | ❌ |
| `OSS_LOCAL_BASE` | 本地缓存路径 | - | ❌ |

### Nacos 配置

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| `NACOS_SERVER_ADDR` | Nacos 服务器地址 | localhost:8848 | ✅ |
| `NACOS_NAMESPACE` | 命名空间 | - | ❌ |
| `NACOS_USERNAME` | Nacos 用户名 | nacos | ❌ |
| `NACOS_PASSWORD` | Nacos 密码 | nacos | ❌ |

## 🔧 不同环境的配置

### 开发环境 (.env)

```env
DB_HOST=localhost
DB_PASSWORD=dev_password
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
```

### 测试环境 (.env.test)

```env
DB_HOST=test-db.example.com
DB_PASSWORD=test_password
OSS_ENDPOINT=oss-cn-shanghai.aliyuncs.com
```

### 生产环境 (Nacos 配置中心)

生产环境建议使用 Nacos 配置中心，而不是 `.env` 文件：

1. 在 Nacos 控制台创建配置文件
2. 设置配置为加密
3. 应用启动时自动从 Nacos 读取

## 🛡️ 安全最佳实践

### 1. 不要提交 .env 文件

确认 `.gitignore` 包含：

```gitignore
.env
.env.local
.env.*.local
*.secret.yml
```

### 2. 使用 .env.example 作为模板

`.env.example` 可以提交到 Git，但只包含占位符：

```env
DB_PASSWORD=your_mysql_password_here  # ← 占位符
```

### 3. 定期轮换密钥

- 每 90 天更换一次数据库密码
- 每 90 天轮换一次 AccessKey
- 立即禁用泄露的密钥

### 4. 最小权限原则

- 数据库账号仅授予必要的权限
- RAM 用户仅授予 OSS 读写权限
- 不要使用 root 或主账号

### 5. 启用审计日志

```yaml
# application.yml
logging:
  level:
    com.yourpackage: INFO
  file:
    name: ./logs/application.log
```

## 📂 项目结构

```
Unmanned_Gym/
├── .env.example              # 环境变量模板（可提交）
├── .env                      # 真实配置（不提交，需手动创建）
├── .gitignore                # Git 忽略规则
├── services/
│   ├── UserService/
│   │   └── src/main/resources/
│   │       └── application.yml  # 使用 ${VAR} 引用环境变量
│   ├── FaceService/
│   │   └── src/main/resources/
│   │       └── application.yml
│   └── ...
└── ENV_SETUP_GUIDE.md        # 本文档
```

## 🔍 验证配置

### 方法 1: 检查环境变量是否生效

```bash
# Windows PowerShell
Get-ChildItem Env: | Select-String "DB_"

# Linux/Mac
env | grep DB_
```

### 方法 2: 启动应用查看日志

启动应用后，检查日志中是否有配置加载的信息：

```
2024-01-15 10:00:00 [main] INFO  o.s.b.f.s.DefaultListableBeanFactory
  - Loading properties from environment variables
```

### 方法 3: 测试连接

创建一个测试接口验证配置：

```java
@RestController
public class ConfigTestController {
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @GetMapping("/test/config")
    public Map<String, String> testConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("dbUrl", dbUrl);
        config.put("status", "OK");
        return config;
    }
}
```

## ❓ 常见问题

### Q1: 应用启动时报错 "Could not resolve placeholder"

**原因**: 环境变量未设置且没有默认值

**解决**: 
```yaml
# 添加默认值
password: ${DB_PASSWORD:default_password}
```

或在 `.env` 中设置该变量。

### Q2: .env 文件没有被自动加载

**原因**: Spring Boot 默认不加载 `.env` 文件

**解决**: 

**方式一**: 使用 dotenv-spring-boot-starter

```xml
<dependency>
    <groupId>me.paulschwarzenberger</groupId>
    <artifactId>dotenv-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

**方式二**: 手动加载

```java
@PostConstruct
public void loadEnv() {
    try {
        Dotenv dotenv = Dotenv.configure().load();
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
    } catch (Exception e) {
        log.warn("Failed to load .env file", e);
    }
}
```

**方式三**: 在 IDE 中配置环境变量

IntelliJ IDEA:
1. Run → Edit Configurations
2. Environment variables → 导入 `.env` 文件

### Q3: 如何在 Docker 中使用环境变量？

创建 `docker-compose.yml`:

```yaml
version: '3.8'
services:
  app:
    image: unmanned-gym:latest
    env_file:
      - .env
    environment:
      - SPRING_PROFILES_ACTIVE=prod
```

### Q4: 如何加密 .env 文件？

使用 git-crypt:

```bash
# 安装 git-crypt
brew install git-crypt  # Mac
sudo apt-get install git-crypt  # Linux

# 初始化
git-crypt init

# 加密 .env 文件
git-crypt encrypt .env

# 解密
git-crypt decrypt .env
```

## 📚 相关文档

- [.env.example](.env.example) - 环境变量模板
- [.gitignore](.gitignore) - Git 忽略规则
- [ALIYUN_OSS_CONFIG_TEMPLATE.md](ALIYUN_OSS_CONFIG_TEMPLATE.md) - 阿里云配置指南
- [SECURITY_REPORT.md](SECURITY_REPORT.md) - 安全报告

## 🔗 参考资源

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Dotenv for Java](https://github.com/cdimascio/dotenv-java)
- [Nacos Configuration Center](https://nacos.io/zh-cn/docs/quick-start.html)

---

**最后更新**: 2024-01-15  
**版本**: 1.0  
**维护者**: Unmanned Gym Team
