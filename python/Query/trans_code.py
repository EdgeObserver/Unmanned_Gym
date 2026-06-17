import os
from openai import OpenAI
from dotenv import load_dotenv

# 1. 加载环境变量
load_dotenv()
api_key = os.getenv("DASHSCOPE_API_KEY")

if not api_key:
    raise ValueError("未找到 API 密钥，请检查环境变量 DASHSCOPE_API_KEY 是否设置正确。")

# 2. 创建客户端，指向通义千问的服务地址
client = OpenAI(
    api_key=api_key,
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1"
)

# 3. 读取你的项目代码
# 这里以读取单个 Python 文件为例，你可以扩展为读取整个文件夹
code_file_path = "your_project_file.py"  # 替换成你的代码文件路径
try:
    with open(code_file_path, "r", encoding="utf-8") as f:
        project_code = f.read()
except FileNotFoundError:
    print(f"错误：找不到文件 {code_file_path}")
    exit()

# 4. 设计提示词 (Prompt)
prompt = f"""
你是一位资深技术专家。请阅读我提供的项目代码，并生成一份项目分析报告。
报告需要包含以下内容：
1.  **项目概述**：简要说明这个项目是做什么的。
2.  **技术栈分析**：识别并列出使用的主要库、框架和技术。
3.  **核心功能与模块**：分析代码的主要函数或类，并解释它们的作用。
4.  **代码风格与优化建议**：评估代码的可读性，并提出 2-3 条具体的优化建议。

【项目代码】
```python
{project_code}"""