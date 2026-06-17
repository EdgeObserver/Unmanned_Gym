# AI助手服务设计说明文档

## 1. 概述

AI助手服务是一个基于Flask的智能问答系统，专门用于健身领域的知识问答。该服务结合了Neo4j图数据库的知识检索和大语言模型（LLM）的生成能力，采用RAG（Retrieval-Augmented Generation）架构，能够提供准确、专业的健身建议。

## 2. 模块架构

### 2.1 技术栈
- **Web框架**: Flask
- **AI模型**: DeepSeek-V3.2 (通过阿里云DashScope API)
- **图数据库**: Neo4j
- **服务注册**: Nacos
- **通信协议**: HTTP/REST + SSE (Server-Sent Events)

### 2.2 分层架构
```
┌─────────────────────┐
│   Flask App Layer   │  ← HTTP接口层，服务注册
├─────────────────────┤
│   QA Engine Layer   │  ← 问题解析，知识检索
├─────────────────────┤
│ AI Connector Layer  │  ← AI模型调用封装
├─────────────────────┤
│ Neo4j Driver Layer  │  ← 图数据库访问
├─────────────────────┤
│ External Services   │  ← OpenAI API, Neo4j DB, Nacos
└─────────────────────┘
```

## 3. 核心组件说明

### 3.1 Flask应用层 (app.py)

#### 主要功能
- **服务入口**: 提供REST API接口
- **服务注册**: 自动注册到Nacos服务发现中心
- **请求处理**: 接收用户问题，协调各组件完成问答

#### 核心接口

##### `/talk` (POST) - 非流式对话
- **功能**: 接收用户问题，返回完整的AI回答
- **流程**: 
  1. 接收问题文本
  2. 调用QA引擎从Neo4j检索知识
  3. 构建增强提示词（整合知识库结果）
  4. 调用AI连接器生成回答
  5. 返回JSON格式响应

##### `/talkStream` (POST) - 流式对话
- **功能**: 实时流式返回AI生成的文字
- **特点**: 
  - 使用SSE (Server-Sent Events) 协议
  - 逐字输出，提升用户体验
  - 支持思考过程展示（可选）

#### 服务注册机制
```python
register_to_nacos():
  1. 连接到Nacos服务器
  2. 注册服务实例（IP:PORT）
  3. 每5秒发送心跳保持活跃
  4. 在后台线程中持续运行
```

### 3.2 AI连接器层 (ai_connecter.py)

#### AIConnector类
**职责**: 封装与大语言模型的交互

**核心方法**:

##### `talk(question: str) -> dict`
- **功能**: 非流式调用AI模型
- **特性**:
  - 启用思考模式 (enable_thinking=True)
  - 收集完整的思考过程 (reasoning_content)
  - 收集最终回答 (answer_content)
  - 统计Token用量 (usage_info)
- **返回格式**:
  ```json
  {
    "code": 200,
    "reasoning": "思考过程...",
    "answer": "最终回答...",
    "usage": {"prompt_tokens": 100, "completion_tokens": 200}
  }
  ```

##### `talkStream(question: str) -> Generator`
- **功能**: 流式调用AI模型
- **特性**:
  - 使用yield逐步返回内容
  - SSE格式输出 (data: {...}\n\n)
  - 支持前端实时渲染
- **输出格式**:
  ```
  data: {"type": "content", "data": "你"}\n\n
  data: {"type": "content", "data": "好"}\n\n
  data: [DONE]\n\n
  ```

**配置信息**:
- **模型**: deepseek-v3.2
- **API地址**: https://dashscope.aliyuncs.com/compatible-mode/v1
- **认证**: API Key方式

### 3.3 问答引擎层 (qa_engine.py)

#### 核心功能
负责理解用户意图，从Neo4j图数据库检索相关知识，生成结构化答案。

#### `parse_question(question: str) -> dict`
**功能**: 自然语言问题解析

**解析步骤**:
1. **文本标准化**: 转小写、去除标点
2. **否定检测**: 识别"不用"、"不要"等否定词
3. **正则模板匹配**: 
   - "用[器械]练[部位]"
   - "[动作]练什么"
   - "练[部位]的[难度]动作"
   - "怎么练[部位]"
4. **同义词匹配**:
   - 肌群同义词 (胸→胸肌, 背→背阔肌)
   - 器械同义词 (徒手→自重, 哑铃→dumbbell)
   - 难度同义词 (新手→初级, 困难→高级)
5. **意图识别**:
   - `list_exercises`: 列出符合条件的动作
   - `exercise_info`: 查询具体动作信息

**返回结构**:
```python
{
  "intent": "list_exercises",
  "muscle": "胸肌",
  "equipment": "哑铃",
  "difficulty": "初级",
  "exercise_name": None,
  "negation": False,
  "raw": "用哑铃练胸的初级动作"
}
```

#### `answer_question(question: str) -> str`
**功能**: 根据解析结果生成答案

**处理逻辑**:

##### 情况1: 查询具体动作信息
```cypher
MATCH (e:Exercise {name: $name})
OPTIONAL MATCH (e)-[:TARGETS]->(m:MuscleGroup)
OPTIONAL MATCH (e)-[:USES]->(eq:Equipment)
RETURN e.name, e.difficulty, e.description, 
       collect(m.name) AS muscles, eq.name AS equipment
```

**输出示例**:
```
「俯卧撑」
- 难度：初级
- 目标肌群：胸肌、三角肌、肱三头肌
- 所需器械：自重
- 说明：经典的上肢推力训练动作...
```

##### 情况2: 列出符合条件的动作
```cypher
MATCH (e:Exercise)
WHERE (e)-[:TARGETS]->(:MuscleGroup {name: $muscle})
  AND (e)-[:USES]->(:Equipment {name: $equipment})
  AND e.difficulty = $difficulty
RETURN e.name, e.difficulty, e.description
ORDER BY e.difficulty
LIMIT 10
```

**支持否定条件**:
```cypher
NOT (e)-[:USES]->(:Equipment {name: $exclude_equipment})
```

**输出示例**:
```
找到 3 个推荐动作：
1. **哑铃卧推**（初级）
   平躺在凳子上，双手持哑铃向上推举...
2. **哑铃飞鸟**（初级）
   双臂微屈，向两侧展开再合拢...
```

#### 同义词词典
```python
MUSCLE_SYNONYMS = {
  "胸肌": ["胸", "胸部", "大胸", "胸大肌", "练胸"],
  "背阔肌": ["背", "背部", "宽背", "练背"],
  "股四头肌": ["大腿前侧", "大腿", "腿部"],
  ...
}

EQUIPMENT_SYNONYMS = {
  "自重": ["徒手", "不用器械", "家里练", "bodyweight"],
  "杠铃": ["barbell", "大杠铃"],
  "哑铃": ["dumbbell", "小哑铃"],
  ...
}

DIFFICULTY_SYNONYMS = {
  "初级": ["新手", "简单", "入门", "初学者"],
  "中级": ["一般", "普通", "中等"],
  "高级": ["高手", "困难", "进阶", "挑战"],
}
```

### 3.4 Neo4j驱动层 (neo4j_driver.py)

#### Neo4jConnection类
**职责**: 管理图数据库连接和查询

**核心方法**:

##### `__init__(uri, user, password)`
- 初始化Neo4j驱动
- 建立数据库连接池

##### `query(query, parameters) -> list`
- 执行Cypher查询
- 参数化查询防止注入
- 返回字典列表结果

##### `close()`
- 关闭数据库连接

**全局实例**:
```python
neo4j_conn = Neo4jConnection(
  uri="bolt://localhost:7687",
  user="neo4j",
  password="7980nospo"
)
```

### 3.5 提示词构建 (build_enhanced_prompt)

**功能**: 将Neo4j检索结果整合到AI提示词中

**策略**:

##### 有知识库结果时
```
你是一个专业的健身助手，请严格基于以下知识库信息回答用户问题。

【健身知识库检索结果】
{neo4j_answer}

──────────────
用户问题：{question}
──────────────

回答要求：
1. 必须优先并主要使用上述知识库信息进行回答
2. 如果知识库信息完整，直接整理输出，不要添加额外内容
3. 如果知识库信息部分相关，先完整输出相关信息，然后可以简要补充通用常识
4. 如果知识库信息与问题不匹配，明确告知用户"知识库中暂无相关信息"
```

##### 无知识库结果时
```
你是一个专业的健身助手。

用户问题：{question}

由于知识库中暂未找到相关信息，请基于你的通用健身知识提供专业建议。
请在回答开头说明："以下是基于通用健身知识的建议："
如果不是健身相关问题，拒绝回答，引导用户提问健身相关问题
```

## 4. 业务流程

### 4.1 完整问答流程

```
用户提问
  ↓
Flask接收请求
  ↓
QA引擎解析问题
  ↓
提取意图、肌群、器械、难度
  ↓
构建Cypher查询
  ↓
Neo4j执行查询
  ↓
返回结构化知识
  ↓
构建增强提示词
  ↓
AI连接器调用LLM
  ↓
流式/非流式返回
  ↓
前端展示答案
```

### 4.2 非流式对话 (/talk)

1. 用户提交问题
2. Flask调用`answer_question()`获取知识库答案
3. 构建增强提示词（整合知识库+用户问题）
4. 调用`AIConnector.talk()`
5. AI模型流式生成（内部），收集完整结果
6. 返回JSON: `{code, reasoning, answer, usage}`
7. 前端一次性显示

### 4.3 流式对话 (/talkStream)

1. 用户提交问题
2. Flask调用`answer_question()`获取知识库答案
3. 构建增强提示词
4. 调用`AIConnector.talkStream()`
5. AI模型流式生成
6. 通过SSE逐块推送给前端
7. 前端实时渲染文字

### 4.4 服务注册流程

1. Flask启动时创建后台线程
2. 调用`register_to_nacos()`
3. 向Nacos注册服务实例
4. 每5秒发送心跳
5. Nacos维护服务健康状态
6. 其他服务可通过Nacos发现本服务

## 5. 数据模型

### 5.1 Neo4j图结构

**节点类型**:
- `Exercise`: 健身动作
  - 属性: name, difficulty, description
- `MuscleGroup`: 肌群
  - 属性: name
- `Equipment`: 器械
  - 属性: name

**关系类型**:
- `[:TARGETS]`: 动作→肌群 (锻炼目标)
- `[:USES]`: 动作→器械 (使用器械)

**示例图谱**:
```
(俯卧撑)-[:TARGETS]->(胸肌)
(俯卧撑)-[:TARGETS]->(三角肌)
(俯卧撑)-[:USES]->(自重)

(哑铃卧推)-[:TARGETS]->(胸肌)
(哑铃卧推)-[:USES]->(哑铃)
```

### 5.2 请求/响应格式

#### /talk 请求
```json
POST /talk
Content-Type: text/plain

用哑铃练胸的初级动作有哪些？
```

#### /talk 响应
```json
{
  "code": 200,
  "reasoning": "用户询问初级哑铃胸部训练动作...",
  "answer": "找到 3 个推荐动作：\n1. **哑铃卧推**...",
  "usage": {
    "prompt_tokens": 150,
    "completion_tokens": 200,
    "total_tokens": 350
  }
}
```

#### /talkStream 响应 (SSE)
```
data: {"type": "content", "data": "找"}\n\n
data: {"type": "content", "data": "到"}\n\n
data: {"type": "content", "data": " "}\n\n
data: {"type": "content", "data": "3"}\n\n
...
data: [DONE]\n\n
```

## 6. 异常处理

### 6.1 常见异常

| 异常类型 | 处理方式 | 用户提示 |
|---------|---------|---------|
| Neo4j连接失败 | catch Exception | "AI 有点困了~再问一次吧" |
| OpenAI API超时 | catch Exception | "AI 有点困了~再问一次吧" |
| 问题解析失败 | 返回空结果 | "请告诉我你想练的部位..." |
| 知识库无匹配 | 返回友好提示 | "没有找到满足条件的动作" |
| 网络错误 | catch Exception | "AI 有点困了~再问一次吧" |

### 6.2 容错机制
- 所有外部调用都有try-catch包裹
- 知识库查询失败不影响AI回答（fallback到通用知识）
- 流式输出中断时发送[DONE]标记

## 7. 性能优化

### 7.1 缓存策略
- Neo4j连接复用（单例模式）
- OpenAI客户端复用
- 可考虑缓存常见问题答案

### 7.2 并发处理
- Flask使用`threaded=True`
- Nacos心跳在独立线程
- 每个请求独立处理

### 7.3 查询优化
- Cypher查询使用参数化
- 限制返回结果数量（LIMIT 10）
- 使用OPTIONAL MATCH避免空值

## 8. 扩展性考虑

### 8.1 功能扩展
- 支持多轮对话上下文
- 增加动作视频链接
- 支持个性化推荐
- 增加用户历史记录

### 8.2 架构扩展
- 引入Redis缓存热点查询
- 使用消息队列异步处理
- 部署多个实例负载均衡
- 增加监控和日志系统

### 8.3 知识库扩展
- 支持更多健身领域（瑜伽、跑步等）
- 增加营养建议
- 集成训练计划生成
- 支持图片识别动作

## 9. 部署配置

### 9.1 环境变量
```bash
DASHSCOPE_API_KEY=sk-xxx
NEO4J_URI=bolt://localhost:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=your_password
NACOS_SERVER=127.0.0.1:8848
```

### 9.2 启动命令
```bash
cd python/Query
python app.py
```

### 9.3 服务信息
- **端口**: 8090
- **服务名**: AssistantService
- **健康检查**: Nacos心跳
- **协议**: HTTP + SSE

## 10. 总结

AI助手服务采用了先进的RAG架构，结合知识图谱的准确性和大语言模型的灵活性，能够提供专业、可靠的健身建议。服务设计模块化、可扩展，支持流式和非流式两种交互方式，并通过Nacos实现服务发现和治理，是一个完善的智能问答系统。