# Unmanned Gym - 无人健身房管理系统

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.5-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Python](https://img.shields.io/badge/Python-3.x-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

一个基于微服务架构的智能化无人健身房管理系统，集成人脸识别、AI问答、人数检测等智能功能

</div>

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [数据库设计](#数据库设计)
- [开发指南](#开发指南)
- [API文档](#api文档)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 🎯 项目简介

Unmanned Gym 是一个现代化的无人健身房管理系统，采用前后端分离的微服务架构设计。系统整合了传统健身房管理功能与人工智能技术，提供用户管理、器材预约、人脸识别签到、实时人数监控、智能问答等全方位服务。

### 核心亮点

- 🏗️ **微服务架构**：基于 Spring Cloud + Nacos 的微服务体系，支持水平扩展
- 🤖 **AI 赋能**：集成人脸识别、YOLO 目标检测、DeepSeek 大语言模型
- 🔐 **安全可靠**：JWT 身份认证 + 网关统一鉴权
- 📊 **实时监控**：WebSocket 实时推送场馆人数数据
- 🎨 **现代化界面**：Vue 3 + Element Plus 构建的管理后台
- 🚀 **高性能**：Redis 缓存 + RabbitMQ 消息队列异步处理

## 💻 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.3.4 | 核心框架 |
| Spring Cloud | 2023.0.3 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.3.2 | 阿里微服务组件 |
| Nacos | - | 服务注册发现与配置中心 |
| MyBatis Plus | 3.5.6 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | - | 缓存数据库 |
| RabbitMQ | - | 消息队列 |
| Gateway | - | API 网关 |
| OpenFeign | - | 服务间调用 |
| JWT | 4.4.0 | 身份认证 |
| Aliyun OSS | 3.15.1 | 对象存储 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.5+ | 渐进式 JavaScript 框架 |
| TypeScript | 6.0+ | 类型安全的 JavaScript 超集 |
| Vite | 8.0+ | 下一代前端构建工具 |
| Element Plus | 2.14+ | Vue 3 组件库 |
| Pinia | 3.0+ | Vue 状态管理 |
| Vue Router | 5.0+ | Vue 官方路由 |
| ECharts | - | 数据可视化图表库 |
| WebSocket | - | 实时通信 |

### AI & Python

| 技术 | 说明 |
|------|------|
| YOLO v5 | 目标检测算法（人数统计） |
| face_recognition | 人脸识别库 |
| DeepSeek API | 大语言模型（智能问答） |
| Neo4j | 知识图谱数据库 |
| OpenCV | 计算机视觉库 |

## 🏛️ 系统架构

```
┌─────────────────────────────────────────────────────┐
│                   前端层 (Vue 3)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │ 用户端   │  │ 管理端   │  │ 教练端   │          │
│  └──────────┘  └──────────┘  └──────────┘          │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP/WebSocket
┌──────────────────────▼──────────────────────────────┐
│              API 网关 (Gateway)                      │
│         路由转发 / 负载均衡 / 鉴权校验               │
└──┬──────┬──────┬──────┬──────┬──────┬──────┬────────┘
   │      │      │      │      │      │      │
┌──▼──┐┌─▼──┐┌─▼──┐┌─▼──┐┌─▼──┐┌─▼──┐┌─▼────┐
│User ││Face││Book││Order││Count││Record││Resource│
│Service││Service││Service││Service││Service││Service││Service│
└──┬──┘└──┬──┘└──┬──┘└──┬──┘└──┬──┘└──┬──┘└──┬────┘
   │      │      │      │      │      │      │
   └──────┴──────┴──┬───┴──────┴──────┴──────┘
                    │ Feign / RabbitMQ
┌───────────────────▼───────────────────────────────┐
│            基础设施层                               │
│  ┌────────┐ ┌──────┐ ┌──────┐ ┌────────┐        │
│  │ MySQL  │ │Redis │ │RabbitMQ│ │Aliyun OSS│    │
│  └────────┘ └──────┘ └──────┘ └────────┘        │
└───────────────────────────────────────────────────┘
                    │ HTTP
┌───────────────────▼───────────────────────────────┐
│           AI 服务层 (Python)                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │人脸识别  │  │人数检测  │  │智能问答  │       │
│  └──────────┘  └──────────┘  └──────────┘       │
└───────────────────────────────────────────────────┘
```

## ✨ 功能特性

### 用户管理模块 (UserService)
- ✅ 用户注册与登录
- ✅ 会员信息管理
- ✅ 权限控制（用户/管理员/教练）
- ✅ 个人信息维护

### 人脸识别模块 (FaceService)
- ✅ 人脸信息采集与注册
- ✅ 人脸识别签到/签退
- ✅ 活体检测防作弊
- ✅ 人脸特征库管理

### 预约管理模块 (BookService)
- ✅ 器材在线预约
- ✅ 课程预约管理
- ✅ 预约冲突检测
- ✅ 预约取消与改期

### 订单管理模块 (OrderService)
- ✅ 会员卡购买
- ✅ 订单生成与管理
- ✅ 支付状态跟踪
- ✅ 订单历史记录

### 人数检测模块 (CountService)
- ✅ 实时人数统计
- ✅ 场馆容量监控
- ✅ 人流趋势分析
- ✅ 超限预警通知

### 出入记录模块 (RecordService)
- ✅ 签到签退记录
- ✅ 出入时间统计
- ✅  attendance 报表生成
- ✅ 异常行为检测

### 资源管理模块 (ResourceService)
- ✅ 健身器材管理
- ✅ 场地资源管理
- ✅ 设备状态监控
- ✅ 维护记录管理

### 知识库模块 (KnowledgeService)
- ✅ 健身知识问答
- ✅ 智能客服对话
- ✅ 知识图谱查询
- ✅ 个性化建议推荐

## 📁 项目结构

```
Unmanned_Gym/
├── gateway/                    # API 网关服务
│   ├── src/main/java/         # 网关核心代码
│   └── pom.xml
├── services/                   # 微服务集合
│   ├── UserService/           # 用户服务
│   ├── FaceService/           # 人脸识别服务
│   ├── BookService/           # 预约服务
│   ├── OrderService/          # 订单服务
│   ├── CountService/          # 人数检测服务
│   ├── RecordService/         # 出入记录服务
│   ├── ResourceService/       # 资源管理服务
│   ├── KnowledgeService/      # 知识库服务
│   └── pom.xml
├── model/                      # 公共数据模型
│   ├── src/main/java/pojo/    # 实体类定义
│   └── pom.xml
├── front/view/                 # 前端项目
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   ├── components/        # 通用组件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # 状态管理
│   │   └── assets/            # 静态资源
│   └── package.json
├── python/                     # Python AI 服务
│   ├── FaceService.py         # 人脸识别服务
│   ├── PersonDetectService.py # 人数检测服务
│   ├── Query/                 # 智能问答服务
│   │   ├── ai_connecter.py    # AI API 连接
│   │   ├── qa_engine.py       # 问答引擎
│   │   └── neo4j_driver.py    # Neo4j 驱动
│   ├── face_recognize.py      # 人脸识别核心
│   └── config.yaml            # Python 配置
├── text/                       # 设计文档
│   ├── 系统架构设计.md
│   ├── 需求分析报告.md
│   └── 接口说明文档.md
├── database_init.sql          # 数据库初始化脚本（完整）
├── database_migration_add_membership_end_time.sql  # 数据库迁移脚本
└── pom.xml                     # Maven 父 POM
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 20.19+ 或 22.12+
- Python 3.8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- Maven 3.6+
- Nacos 2.x

### 安装步骤

#### 1. 克隆项目

```bash
git clone https://github.com/your-username/Unmanned_Gym.git
cd Unmanned_Gym
```

#### 2. 启动基础设施

```bash
# 启动 Nacos（默认端口 8848）
sh nacos/bin/startup.sh -m standalone

# 启动 MySQL
# 确保 MySQL 服务运行，并创建数据库
mysql -u root -p
CREATE DATABASE unmanned_gym DEFAULT CHARACTER SET utf8mb4;

# 启动 Redis
redis-server

# 启动 RabbitMQ
rabbitmq-server
```

#### 3. 初始化数据库

**方式一：使用完整初始化脚本（推荐）**

```bash
# 执行完整的数据库初始化脚本（包含表结构和初始数据）
mysql -u root -p < database_init.sql
```

该脚本会自动：
- ✅ 创建 `unmanned_gym` 数据库
- ✅ 创建所有业务表（10张表）
- ✅ 添加索引和外键约束
- ✅ 创建视图和触发器
- ✅ 插入初始数据（管理员、时间段、套餐、器材）

**方式二：手动创建**

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE unmanned_gym DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unmanned_gym;

# 执行初始化脚本
source database_init.sql;
```

**默认账号信息：**

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 需要在代码中进行 BCrypt 加密 |

**数据库表结构概览：**

| 表名 | 中文名 | 说明 |
|------|--------|------|
| `user` | 用户表 | 存储健身房用户基本信息 |
| `manager` | 管理员表 | 存储系统管理员信息 |
| `coach` | 教练表 | 存储健身教练信息 |
| `package` | 套餐表 | 存储会员卡套餐信息 |
| `order` | 订单表 | 存储用户购买订单记录 |
| `equipment` | 器材表 | 存储健身器材信息 |
| `time_slot` | 时间段表 | 存储预约时间段配置 |
| `equipment_booking` | 器材预约表 | 存储用户器材预约记录 |
| `coach_booking` | 教练预约表 | 存储用户教练预约记录 |
| `action_record` | 出入记录表 | 存储用户进出健身房记录 |

**数据库特性：**

- 🔐 **数据安全**：使用 utf8mb4 字符集，支持 emoji 等特殊字符
- ⚡ **性能优化**：关键字段已添加索引，提升查询效率
- 🔗 **数据完整性**：外键约束保证数据关联正确性
- 📊 **统计视图**：提供会员状态、设备可用性等常用视图
- ⚙️ **自动触发**：预约时自动更新器材可用数量

### 数据库设计文档

详细的数据库设计文档请参考：[DATABASE_DESIGN.md](DATABASE_DESIGN.md)

文档包含：
- 📋 完整的表结构说明（10张表）
- 🔑 索引设计和优化建议
- 🔗 外键关系和ER图
- 👁️ 视图设计和使用示例
- ⚙️ 触发器逻辑说明
- 💡 常见查询示例
- 🔐 数据安全和备份策略

#### 4. 启动后端服务

```bash
# 编译整个项目
mvn clean install

# 启动各个微服务（按顺序）
# 在每个服务目录下执行
mvn spring-boot:run

# 或者使用 IDE 直接运行各服务的 Application 主类
# 建议启动顺序：
# 1. UserService
# 2. FaceService
# 3. BookService
# 4. OrderService
# 5. CountService
# 6. RecordService
# 7. ResourceService
# 8. KnowledgeService
# 9. Gateway（最后启动）
```

#### 5. 启动 Python AI 服务

```bash
cd python

# 安装依赖
pip install flask opencv-python face-recognition yolo neo4j requests pyyaml

# 启动人脸识别服务
python FaceService.py

# 启动人数检测服务
python PersonDetectService.py

# 启动智能问答服务
cd Query
python app.py
```

#### 6. 启动前端

```bash
cd front/view

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

### 配置文件说明

#### 后端配置 (application.yml)

每个微服务需要配置以下关键信息：

```yaml
server:
  port: 8081  # 服务端口

spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848  # Nacos 地址
      config:
        server-addr: 127.0.0.1:8848
  
  datasource:
    url: jdbc:mysql://localhost:3306/unmanned_gym
    username: root
    password: your_password
  
  redis:
    host: localhost
    port: 6379
  
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

#### Python 配置 (config.yaml)

```yaml
face_service:
  host: 0.0.0.0
  port: 5001
  
person_detect:
  host: 0.0.0.0
  port: 5002
  model_path: yolov5nu.pt
  
query_service:
  host: 0.0.0.0
  port: 5003
  deepseek_api_key: your_api_key
  neo4j_uri: bolt://localhost:7687
```

## 📖 开发指南

### 添加新微服务

1. 在 `services/` 目录下创建新的服务模块
2. 继承父 POM 的依赖配置
3. 创建标准的三层架构：Controller、Service、Mapper
4. 在 Nacos 中注册服务
5. 通过 Feign 客户端与其他服务通信

### 前端开发规范

- 使用 Composition API 编写组件
- 遵循 TypeScript 类型规范
- 使用 ESLint + Prettier 保持代码风格一致
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case

### Python AI 服务开发

- 遵循 PEP 8 编码规范
- 使用 Flask 构建 RESTful API
- 模型文件统一存放在 `python/` 根目录
- 配置信息统一在 `config.yaml` 中管理

### 数据库设计规范

- 表名使用小写 + 下划线
- 主键统一使用 `id` (INT AUTO_INCREMENT)
- 必须包含审计字段：`created_by`, `created_time`, `updated_by`, `updated_time`, `is_deleted`, `status`
- 字符集统一使用 `utf8mb4_unicode_ci`
- 所有表必须有注释说明
- 外键关联建议设置物理外键约束（开发环境可省略以提升性能）

### 数据库 ER 图

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    user      │       │   manager    │       │    coach     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)      │
│ username     │       │ username     │       │ name         │
│ password     │       │ password     │       │ email        │
│ email        │       └──────────────┘       │ user_pic     │
│ user_pic     │                              └──────────────┘
│ in_place     │                                     ▲
│ membership_  │                                     │
│   end_time   │                              ┌──────┴──────┐
└───────┬──────┘                              │coach_booking│
        │                                     ├─────────────┤
        │                                     │ id (PK)     │
        │         ┌──────────────┐            │ user_id(FK) │
        │         │action_record │            │ coach_id(FK)│
        │         ├──────────────┤            │booking_date │
        │         │ id (PK)      │            │ slot_id(FK) │
        │         │ user_id(FK)  │            └─────────────┘
        │         │arrival_time  │
        │         │departure_time│                    ▲
        └─────────┴──────────────┘                    │
                                                      │
┌──────────────┐       ┌──────────────┐              │
│   package    │       │    order     │              │
├──────────────┤       ├──────────────┤              │
│ id (PK)      │◄──────│ id (PK)      │              │
│ name         │       │ uid (FK)     │              │
│ price        │       │ pid (FK)     │              │
│ level        │       │order_start   │              │
│ duration     │       │order_end     │              │
└──────────────┘       └──────────────┘              │
                                                      │
┌──────────────┐       ┌──────────────┐              │
│  equipment   │       │time_slot     │              │
├──────────────┤       ├──────────────┤              │
│ id (PK)      │       │ id (PK)      │              │
│ name         │       │ slot_name    │              │
│ num          │       │ start_time   │              │
│ spare        │       │ end_time     │              │
│ need_coach   │       └──────┬───────┘              │
└───────┬──────┘              │                      │
        │                     │                      │
        │         ┌───────────┴───────────┐          │
        │         │ equipment_booking     │          │
        │         ├───────────────────────┤          │
        └────────►│ id (PK)               │          │
                  │ user_id (FK) ─────────┼──────────┘
                  │ equipment_id(FK)      │
                  │ booking_date          │
                  │ slot_id (FK)          │
                  └───────────────────────┘
```

## 📡 API 文档

### 主要接口概览

| 服务 | 基础路径 | 说明 |
|------|---------|------|
| UserService | `/api/user` | 用户管理相关接口 |
| FaceService | `/api/face` | 人脸识别相关接口 |
| BookService | `/api/book` | 预约管理相关接口 |
| OrderService | `/api/order` | 订单管理相关接口 |
| CountService | `/api/count` | 人数统计相关接口 |
| RecordService | `/api/record` | 出入记录相关接口 |
| ResourceService | `/api/resource` | 资源管理相关接口 |
| KnowledgeService | `/api/knowledge` | 智能问答相关接口 |

详细的 API 文档请参考：[接口说明文档.md](text/接口说明文档.md)

### 示例：用户登录

```bash
POST http://localhost:8080/api/user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}

# 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "role": "USER"
    }
  }
}
```

## 🤝 贡献指南

我们欢迎任何形式的贡献！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 贡献规范

- 遵循现有的代码风格
- 添加必要的单元测试
- 更新相关文档
- 确保 CI/CD 检查通过

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 团队成员

- 项目负责人：[Your Name]
- 后端开发：Backend Team
- 前端开发：Frontend Team
- AI 算法：AI Team

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 💬 Issues: [GitHub Issues](https://github.com/your-username/Unmanned_Gym/issues)

## 🙏 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [YOLO](https://github.com/ultralytics/yolov5)
- [face_recognition](https://github.com/ageitgey/face_recognition)
- [DeepSeek](https://www.deepseek.com/)

---

<div align="center">

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！⭐

Made with ❤️ by Unmanned Gym Team

</div>
