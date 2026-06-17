# 🚀 快速开始指南

> 5 分钟快速启动 Unmanned Gym 项目

## 📋 前置要求

- ✅ JDK 17+
- ✅ Maven 3.6+
- ✅ MySQL 8.0+
- ✅ Redis 6.0+
- ✅ RabbitMQ 3.8+
- ✅ Node.js 20.19+（前端）
- ✅ Python 3.8+（AI 服务）

---

## ⚡ 快速启动（3 步）

### 步骤 1: 配置环境变量

```powershell
# 复制配置模板
Copy-Item .env.example .env

# 编辑配置文件
notepad .env
```

填写真实的配置信息：

```env
# 数据库
DB_PASSWORD=your_mysql_password

# Redis
REDIS_PASSWORD=your_redis_password

# RabbitMQ
RABBITMQ_PASSWORD=your_rabbitmq_password

# 阿里云 OSS
ALIYUN_ACCESS_KEY_ID=LTAI5t...
ALIYUN_ACCESS_KEY_SECRET=your-secret-key
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET_NAME=your-bucket-name
```

验证配置：

```powershell
.\verify_env.ps1
```

### 步骤 2: 初始化数据库

```bash
# 执行数据库初始化脚本
mysql -u root -p < database_init.sql
```

**默认账号**：
- 管理员：admin / admin123

### 步骤 3: 启动服务

**方式一：使用快速启动脚本**

```powershell
.\quick_start.ps1
```

**方式二：手动启动**

```bash
# 后端服务（按顺序启动）
cd services/UserService
mvn spring-boot:run

cd services/FaceService
mvn spring-boot:run

# ... 其他服务

# 前端
cd front/view
npm install
npm run dev
```

---

## 📖 详细启动步骤

### 1. 启动基础设施

#### MySQL

```bash
# Windows（假设已安装）
net start MySQL80

# Linux
sudo systemctl start mysql
```

创建数据库（如果未执行初始化脚本）：

```sql
CREATE DATABASE unmanned_gym DEFAULT CHARACTER SET utf8mb4;
```

#### Redis

```bash
# Windows
redis-server

# Linux
sudo systemctl start redis
```

#### RabbitMQ

```bash
# Windows
rabbitmq-server

# Linux
sudo systemctl start rabbitmq-server
```

#### Nacos

```bash
# 下载 Nacos
wget https://github.com/alibaba/nacos/releases/download/2.3.0/nacos-server-2.3.0.zip

# 解压并启动
unzip nacos-server-2.3.0.zip
cd nacos/bin
startup.cmd -m standalone  # Windows
sh startup.sh -m standalone  # Linux
```

访问 Nacos 控制台：http://localhost:8848/nacos  
默认账号：nacos / nacos

### 2. 初始化数据库

```bash
mysql -u root -p < database_init.sql
```

**包含内容**：
- ✅ 10 张核心业务表
- ✅ 索引和外键约束
- ✅ 视图和触发器
- ✅ 初始数据（管理员、时间段、套餐、器材）

**验证**：

```sql
USE unmanned_gym;
SHOW TABLES;
SELECT * FROM manager;
```

### 3. 配置环境变量

参考 [ENV_SETUP_GUIDE.md](ENV_SETUP_GUIDE.md) 详细配置。

**必需配置项**：

```env
DB_PASSWORD=your_password
REDIS_PASSWORD=your_password
RABBITMQ_PASSWORD=your_password
ALIYUN_ACCESS_KEY_ID=your_key_id
ALIYUN_ACCESS_KEY_SECRET=your_key_secret
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
OSS_BUCKET_NAME=your-bucket
```

**验证配置**：

```powershell
.\verify_env.ps1
```

### 4. 启动后端服务

**推荐启动顺序**：

1. UserService（用户服务）
2. FaceService（人脸识别）
3. BookService（预约服务）
4. OrderService（订单服务）
5. CountService（人数检测）
6. RecordService（出入记录）
7. ResourceService（资源管理）
8. KnowledgeService（知识库）
9. Gateway（网关，最后启动）

**启动命令**：

```bash
# 在每个服务目录下执行
cd services/UserService
mvn clean package
mvn spring-boot:run

# 或使用 IDE 直接运行 Application 主类
```

**验证服务**：

访问 Nacos 控制台查看服务注册情况：  
http://localhost:8848/nacos → 服务管理 → 服务列表

应该看到所有服务都已注册。

### 5. 启动 Python AI 服务

```bash
cd python

# 安装依赖
pip install flask opencv-python face-recognition neo4j requests pyyaml

# 启动人脸识别服务
python FaceService.py

# 启动人数检测服务
python PersonDetectService.py

# 启动智能问答服务
cd Query
python app.py
```

**验证**：

```bash
curl http://localhost:5000/health
```

### 6. 启动前端

```bash
cd front/view

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问：http://localhost:5173

---

## ✅ 验证清单

启动完成后，检查以下项：

- [ ] MySQL 数据库已创建并初始化
- [ ] Redis 服务正常运行
- [ ] RabbitMQ 服务正常运行
- [ ] Nacos 控制台可访问
- [ ] 所有微服务已在 Nacos 注册
- [ ] Python AI 服务正常响应
- [ ] 前端页面可访问
- [ ] 能够成功登录（admin / admin123）

---

## 🔧 常见问题

### Q1: 服务启动失败，提示 "Could not resolve placeholder"

**原因**：环境变量未正确加载

**解决**：
1. 确认 `.env` 文件存在
2. 运行 `.\verify_env.ps1` 检查配置
3. 在 IDE 中配置环境变量加载（安装 EnvFile 插件）

### Q2: 无法连接数据库

**原因**：数据库密码错误或数据库未创建

**解决**：
```bash
# 检查数据库是否存在
mysql -u root -p -e "SHOW DATABASES;" | grep unmanned_gym

# 重新初始化
mysql -u root -p < database_init.sql
```

### Q3: Nacos 中看不到服务

**原因**：服务未成功注册

**解决**：
1. 检查 Nacos 是否启动：http://localhost:8848/nacos
2. 查看服务日志是否有错误
3. 确认 `application.yml` 中 Nacos 地址正确

### Q4: 前端页面空白

**原因**：后端服务未启动或 CORS 问题

**解决**：
1. 确认所有后端服务已启动
2. 检查浏览器控制台错误信息
3. 确认 Gateway 服务已启动

---

## 📚 相关文档

- [README.md](README.md) - 项目完整文档
- [ENV_SETUP_GUIDE.md](ENV_SETUP_GUIDE.md) - 环境变量配置详解
- [DATABASE_DESIGN.md](DATABASE_DESIGN.md) - 数据库设计文档
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - 安全配置指南

---

## 💡 下一步

1. **浏览功能**
   - 用户注册登录
   - 人脸识别签到
   - 器材预约
   - 查看数据统计

2. **开发新功能**
   - 阅读 [README.md](README.md) 中的开发指南
   - 了解项目架构
   - 添加新的微服务

3. **部署到生产环境**
   - 使用 Nacos 配置中心
   - 配置 HTTPS
   - 设置监控告警

---

**祝您使用愉快！** 🎉

如有问题，请查阅文档或提交 Issue。
