import time
import json
import os
from flask import Flask, jsonify, request, Response
# from v2.nacos import NacosNamingService, ClientConfigBuilder, GRPCConfig, Instance, SubscribeServiceParam, \
#     RegisterInstanceParam, DeregisterInstanceParam, BatchRegisterInstanceParam, GetServiceParam, ListServiceParam, \
#     ListInstanceParam, NacosConfigService, ConfigParam

import nacos
import threading

from ai_connecter import AIConnector
from qa_engine import parse_question, answer_question
from neo4j_driver import neo4j_conn
app = Flask(__name__)

# Nacos服务注册配置
NACOS_SERVER_ADDR = "127.0.0.1:8848"
NAMESPACE = "public"
SERVICE_NAME = "AssistantService"
CLUSTER_NAME = "DEFAULT"
INSTANCE_IP = "127.0.0.1"
INSTANCE_PORT = 8090

def build_enhanced_prompt(question: str, neo4j_answer: str) -> str:
    """
    构建增强的提示词，将 Neo4j 查询结果整合到问题中
    """
    if neo4j_answer and "没有找到" not in neo4j_answer and "请告诉我" not in neo4j_answer:
        prompt = f"""你是一个专业的健身助手，请**严格基于以下知识库信息**回答用户问题。

【健身知识库检索结果】
{neo4j_answer}

──────────────
用户问题：{question}
──────────────

回答要求：
1. **必须优先并主要使用上述知识库信息**进行回答
2. 如果知识库信息完整，直接整理输出，不要添加额外内容
3. 如果知识库信息部分相关，先完整输出相关信息，然后可以简要补充通用常识（需注明"补充说明"）
4. 如果知识库信息与问题不匹配，明确告知用户"知识库中暂无相关信息"
5. 保持回答结构清晰、专业友好
6. 涉及动作时，保留原文的关键细节（难度、肌群、器械等）
"""
    else:
        prompt = f"""你是一个专业的健身助手。

用户问题：{question}

由于知识库中暂未找到相关信息，请基于你的通用健身知识提供专业建议。
请在回答开头说明："以下是基于通用健身知识的建议："
如果不是减少相关问题，拒绝回答，引导用户提问健身相关问题
要求：
1. 提供科学、安全的健身指导
2. 结构清晰，易于理解
3. 如有必要，提醒用户注意动作规范和安全
"""

    return prompt
@app.route('/talk', methods=['POST'])
def talk():
    try:
        raw_data = request.get_data()
        print(raw_data)
        # 将字节字符串解码为字符串
        question = raw_data.decode('utf-8')
        print(question)

        # 步骤 1: 从 Neo4j 查询并生成答案
        neo4j_answer = answer_question(question)
        print(f"Neo4j 答案：{neo4j_answer}")

        # 步骤 2: 构建增强的提示词
        enhanced_question = build_enhanced_prompt(question, neo4j_answer)

        # 步骤 3: 调用 AI 连接器（不做任何修改）
        connector = AIConnector()
        result_msg = connector.talk(enhanced_question)

    except Exception as e:
        # 其他异常
        print(f"发生错误：{str(e)}")
        result_msg = {
            "code": 500,
            "data": 'AI 有点困了~再问一次吧',
            "msg": "对话失败",
        }

    return jsonify(result_msg), 200


@app.route('/talkStream', methods=['POST'])
def talkStream():
    try:
        raw_data = request.get_data()
        # 将字节字符串解码为字符串
        question = raw_data.decode('utf-8')
        print(f"收到流式请求：{question}")

        # 步骤 1: 从 Neo4j 查询并生成答案
        neo4j_answer = answer_question(question)
        print(f"Neo4j 答案：{neo4j_answer}")

        # 步骤 2: 构建增强的提示词
        enhanced_question = build_enhanced_prompt(question, neo4j_answer)

        connector = AIConnector()

        # 步骤 3: 调用 AI 连接器（不做任何修改）
        return Response(
            connector.talkStream(enhanced_question),
            mimetype="text/event-stream",
            headers={
                'Cache-Control': 'no-cache',
                'Connection': 'keep-alive',
                'X-Accel-Buffering': 'no',
                'Content-Type': 'text/event-stream; charset=utf-8'
            },
            direct_passthrough=True
        )

    except Exception as e:
        print(f"流式对话失败：{str(e)}")
        # 发生错误时返回错误响应
        return jsonify({
            "code": 500,
            "data": f'AI 有点困了~再问一次吧 (错误：{str(e)})',
            "msg": "流式对话失败",
        }), 500
# @app.route('/python/talkStream', methods=['POST'])
# def talkStream():
#     raw_data = request.get_data()
#     # 将字节字符串解码为字符串
#     question = raw_data.decode('utf-8')
#
#     # 将字符串转换为字典
#
#
#     connector = Connector()
#     # result_data = connector.talkStream(ai_id, question)
#
#     return Response(
#         connector.talkStream(question), mimetype="application/octet-stream"
#     )

# 服务注册函数
# 服务注册和心跳函数
def register_to_nacos():
    client = nacos.NacosClient(NACOS_SERVER_ADDR, namespace=NAMESPACE)
    client.add_naming_instance(
        service_name=SERVICE_NAME,
        ip=INSTANCE_IP,
        port=INSTANCE_PORT,
        cluster_name=CLUSTER_NAME,
        ephemeral=True  # 必须设置为临时实例（依赖心跳）
    )

    while True:
        # 显式发送心跳
        client.send_heartbeat(
            service_name=SERVICE_NAME,
            ip=INSTANCE_IP,
            port=INSTANCE_PORT,
            cluster_name=CLUSTER_NAME
        )
        time.sleep(5)  # 间隔建议5秒（小于Nacos默认15秒超时）

        # 显式发送心跳
        client.send_heartbeat(
            service_name=SERVICE_NAME,
            ip=INSTANCE_IP,
            port=INSTANCE_PORT,
            cluster_name=CLUSTER_NAME
        )
        time.sleep(5)  # 间隔建议5秒（小于Nacos默认15秒超时）


if __name__ == '__main__':
    app.config['JSON_AS_ASCII'] = False
    # 在后台线程中注册服务
    threading.Thread(target=register_to_nacos).start()
    # 启动Flask应用
    app.run(host='0.0.0.0', port=INSTANCE_PORT, debug=True)
