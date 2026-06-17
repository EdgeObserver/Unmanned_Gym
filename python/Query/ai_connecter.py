# ... existing code ...

from openai import OpenAI
import os
import json

class AIConnector:
    def __init__(self):
        self.api_key = "sk-ec5aeccf703d46a397d6f5f8b41f0d49"
        # 初始化 OpenAI 客户端
        self.client = OpenAI(
            # 如果没有配置环境变量，请用阿里云百炼 API Key 替换：api_key="sk-xxx"
            # api_key=os.getenv("DASHSCOPE_API_KEY"),
            api_key=self.api_key,
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )
        self.model = "deepseek-v4-flash"

    # ... existing code ...

    def talk(self, question: str) -> dict:
        """
        接收问题，返回包含思考过程和最终回答的字典。
        返回格式：{"reasoning": "...", "answer": "...", "usage": {...}}
        """
        messages = [{"role": "user", "content": question}]

        completion = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            # 通过 extra_body 设置 enable_thinking 开启思考模式
            extra_body={"enable_thinking": True},
            stream=True,
            stream_options={
                "include_usage": True
            },
        )

        reasoning_content = ""  # 完整思考过程
        answer_content = ""  # 完整回复
        is_answering = False  # 是否进入回复阶段
        usage_info = None

        print("\n" + "=" * 20 + "思考过程" + "=" * 20 + "\n")

        for chunk in completion:
            if not chunk.choices:
                # 获取用量信息
                if hasattr(chunk, 'usage') and chunk.usage:
                    # 将 CompletionUsage 对象转换为字典，以便 Flask jsonify 序列化
                    usage_info = chunk.usage.model_dump() if hasattr(chunk.usage, 'model_dump') else dict(chunk.usage)
                    print("\n" + "=" * 20 + "Token 消耗" + "=" * 20 + "\n")
                    print(usage_info)
                continue

            delta = chunk.choices[0].delta

            # 只收集思考内容
            if hasattr(delta, "reasoning_content") and delta.reasoning_content is not None:
                if not is_answering:
                    print(delta.reasoning_content, end="", flush=True)
                reasoning_content += delta.reasoning_content

            # 收到 content，开始进行回复
            if hasattr(delta, "content") and delta.content:
                if not is_answering:
                    print("\n" + "=" * 20 + "完整回复" + "=" * 20 + "\n")
                    is_answering = True
                print(delta.content, end="", flush=True)
                answer_content += delta.content

        return {
            "code": 200,
            "reasoning": reasoning_content,
            "answer": answer_content,
            "usage": usage_info
        }

    def talkStream(self, question: str):
        """
        流式输出生成器，逐步返回 AI 的回复内容。
        使用 yield 逐步返回数据块，支持 SSE 或流式响应。
        """
        messages = [{"role": "user", "content": question}]

        completion = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
            extra_body={"enable_thinking": True},
            stream=True,
            stream_options={
                "include_usage": True
            },
        )

        for chunk in completion:
            if not chunk.choices:
                # 如果有 usage 信息，也返回给前端
                if hasattr(chunk, 'usage') and chunk.usage:
                    usage_info = chunk.usage.model_dump() if hasattr(chunk.usage, 'model_dump') else dict(chunk.usage)
                    data_str = f"data: {json.dumps({'type': 'usage', 'data': usage_info}, ensure_ascii=False)}\n\n"
                    yield data_str.encode('utf-8')
                continue

            delta = chunk.choices[0].delta

            # 优先返回 content 内容（最终回复）
            if hasattr(delta, "content") and delta.content:
                data_str = f"data: {json.dumps({'type': 'content', 'data': delta.content}, ensure_ascii=False)}\n\n"
                yield data_str.encode('utf-8')

            # 如果需要也返回思考过程，可以取消下面的注释
            # if hasattr(delta, "reasoning_content") and delta.reasoning_content:
            #     data_str = f"data: {json.dumps({'type': 'reasoning', 'data': delta.reasoning_content}, ensure_ascii=False)}\n\n"
            #     yield data_str.encode('utf-8')

        # 发送结束标记
        yield b"data: [DONE]\n\n"

# ... existing code ...
