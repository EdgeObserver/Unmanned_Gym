# 🔧 配置文件敏感信息批量替换指南

## ⚠️ 重要提示

除了阿里云 AccessKey 外，项目中还有以下敏感信息需要处理：

### 发现的敏感信息类型

1. **MySQL 数据库密码**: `4114`
2. **RabbitMQ 密码**: `guest`, `414406`
3. **Redis 密码**: `root@123456`

## 📋 需要修改的文件清单

以下所有 `application.yml` 文件中的密码都需要替换：

| 文件路径 | MySQL密码 | RabbitMQ密码 | Redis密码 |
|---------|----------|--------------|-----------|
| services/BookService/src/main/resources/application.yml | 4114 | - | - |
| services/CountService/src/main/resources/application.yml | 4114 | guest | - |
| services/FaceService/src/main/resources/application.yml | 4114 | guest | - |
| services/KnowledgeService/src/main/resources/application.yml | 4114 | - | - |
| services/OrderService/src/main/resources/application.yml | 4114 | - | - |
| services/RecordService/src/main/resources/application.yml | 4114 | guest | - |
| services/ResourceService/src/main/resources/application.yml | 4114 | - | - |
| services/UserService/src/main/resources/application.yml | 4114 | 414406 | root@123456 |
| src/main/resources/application.yml | 4114 | - | - |

## 🔧 替换方法

### 方法一：使用 IDE 批量替换（推荐）

#### IntelliJ IDEA / WebStorm

1. 按 `Ctrl + Shift + R` 打开全局替换
2. 设置搜索范围：`Project Files`
3. 文件掩码：`*.yml`
4. 执行以下替换：

**MySQL 密码**:
- 查找: `password: 4114`
- 替换为: `password: ${DB_PASSWORD}`

**RabbitMQ 密码**:
- 查找: `password: guest`
- 替换为: `password: ${RABBITMQ_PASSWORD}`
- 查找: `password: 414406`
- 替换为: `password: ${RABBITMQ_PASSWORD}`

**Redis 密码**:
- 查找: `password: root@123456`
- 替换为: `password: ${REDIS_PASSWORD}`

5. 点击 "Replace All"

#### VS Code

1. 按 `Ctrl + Shift + H` 打开全局替换
2. 在 "files to include" 中输入: `**/*.yml`
3. 执行相同的替换操作

### 方法二：使用 PowerShell 脚本批量替换

创建并运行以下 PowerShell 脚本：

```powershell
# replace_passwords.ps1

$files = Get-ChildItem -Recurse -Include application.yml |
         Where-Object { $_.FullName -notmatch '\\target\\' -and $_.FullName -notmatch '\\node_modules\\' }

foreach ($file in $files) {
    Write-Host "处理文件: $($file.FullName)" -ForegroundColor Cyan
    
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    
    # 替换 MySQL 密码
    $content = $content -replace 'password: 4114', 'password: ${DB_PASSWORD}'
    
    # 替换 RabbitMQ 密码
    $content = $content -replace 'password: guest', 'password: ${RABBITMQ_PASSWORD}'
    $content = $content -replace 'password: 414406', 'password: ${RABBITMQ_PASSWORD}'
    
    # 替换 Redis 密码
    $content = $content -replace 'password: root@123456', 'password: ${REDIS_PASSWORD}'
    
    Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
    
    Write-Host "  ✅ 完成" -ForegroundColor Green
}

Write-Host ""
Write-Host "所有文件处理完成！" -ForegroundColor Green
```

**使用方法**:
```powershell
cd D:\Code\GithubProject\Unmanned_Gym
.\replace_passwords.ps1
```

### 方法三：使用 sed 命令（Linux/Mac）

```bash
# 查找所有 application.yml 文件
find . -name "application.yml" -not -path "*/target/*" -not -path "*/node_modules/*" | while read file; do
    echo "处理文件: $file"
    
    # 替换密码
    sed -i '' 's/password: 4114/password: ${DB_PASSWORD}/g' "$file"
    sed -i '' 's/password: guest/password: ${RABBITMQ_PASSWORD}/g' "$file"
    sed -i '' 's/password: 414406/password: ${RABBITMQ_PASSWORD}/g' "$file"
    sed -i '' 's/password: root@123456/password: ${REDIS_PASSWORD}/g' "$file"
    
    echo "  ✅ 完成"
done

echo "所有文件处理完成！"
```

## 📝 替换后的配置示例

### 替换前
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/unmanned_gym
    username: root
    password: 4114
  
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  
  data:
    redis:
      host: localhost
      port: 6379
      password: root@123456
```

### 替换后
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/unmanned_gym
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
  
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD}
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
```

**说明**: `${VAR:default}` 语法表示如果环境变量未设置，则使用默认值。

## 🔐 设置环境变量

### Windows (PowerShell)

```powershell
# 数据库
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"

# RabbitMQ
$env:RABBITMQ_USERNAME="guest"
$env:RABBITMQ_PASSWORD="your_rabbitmq_password"

# Redis
$env:REDIS_PASSWORD="your_redis_password"
```

### Linux/Mac

```bash
# 数据库
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password

# RabbitMQ
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=your_rabbitmq_password

# Redis
export REDIS_PASSWORD=your_redis_password
```

### 永久设置（Windows）

```powershell
# 用户级别环境变量
[Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_password", "User")
[Environment]::SetEnvironmentVariable("RABBITMQ_PASSWORD", "your_password", "User")
[Environment]::SetEnvironmentVariable("REDIS_PASSWORD", "your_password", "User")
```

### 永久设置（Linux/Mac）

添加到 `~/.bashrc` 或 `~/.zshrc`:

```bash
# Database
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password

# RabbitMQ
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=your_rabbitmq_password

# Redis
export REDIS_PASSWORD=your_redis_password
```

然后执行:
```bash
source ~/.bashrc  # 或 source ~/.zshrc
```

## 📦 使用 .env 文件（推荐开发环境）

### 1. 创建 .env 文件

在项目根目录创建 `.env` 文件：

```env
# Database
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=your_rabbitmq_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Aliyun OSS
ALIYUN_ACCESS_KEY_ID=your_access_key_id
ALIYUN_ACCESS_KEY_SECRET=your_access_key_secret
```

### 2. 确保 .env 在 .gitignore 中

检查 `.gitignore` 文件包含：
```gitignore
.env
.env.local
.env.*.local
```

### 3. 加载 .env 文件

**Spring Boot 自动支持**:
Spring Boot 会自动读取 `.env` 文件中的环境变量。

**或者使用 dotenv 库**:
```java
// 在应用启动时加载
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

## ✅ 验证替换结果

### 1. 运行安全检查脚本

```powershell
.\check_secrets.ps1
```

应该看到类似输出：
```
检查 明文密码... ✅ 通过
检查 Secret Key... ✅ 通过
```

### 2. 启动服务测试

启动任意一个微服务，检查是否能正常连接数据库、RabbitMQ 和 Redis。

### 3. 查看日志

确认没有密码相关的错误信息。

## ⚠️ 注意事项

1. **备份配置文件**: 在批量替换前先备份
   ```powershell
   Copy-Item -Path "services/*/src/main/resources/application.yml" -Destination "backup/" -Recurse
   ```

2. **测试环境优先**: 先在测试环境验证，确认无误后再应用到生产环境

3. **团队协作**: 通知团队成员更新本地配置

4. **文档更新**: 更新项目文档，说明如何配置环境变量

5. **CI/CD 配置**: 在 CI/CD 系统中配置相应的环境变量

## 🔄 回滚方法

如果替换后出现问题，可以快速回滚：

```powershell
# 恢复备份
Copy-Item -Path "backup/*/application.yml" -Destination "services/*/src/main/resources/" -Recurse -Force
```

## 📚 相关文档

- [ALIYUN_OSS_CONFIG.md](ALIYUN_OSS_CONFIG.md) - 阿里云 OSS 配置
- [SECURITY_REPORT.md](SECURITY_REPORT.md) - 安全报告
- [.gitignore](.gitignore) - Git 忽略规则

---

**最后更新**: 2024-01-15  
**版本**: 1.0
