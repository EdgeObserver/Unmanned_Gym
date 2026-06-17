# main.py
from flask import Flask, request, jsonify, render_template_string
from qa_engine import answer_question

app = Flask(__name__)


# @app.route("/", methods=['GET'])
# def chat_page():
#     return render_template_string("""
#     <!DOCTYPE html>
#     <html>
#         <head>
#             <title>Chat</title>
#         </head>
#         <body>
#             <h1>Q&A System</h1>
#             <form action="/ask" method="get">
#                 <label for="question">Question:</label><br>
#                 <input type="text" id="question" name="question"><br><br>
#                 <input type="submit" value="Submit">
#             </form>
#         </body>
#     </html>
#     """)

@app.route("/ask", methods=['POST'])
def ask_question_post():
    data = request.get_json()
    question = data.get('question')
    answer = answer_question(question)
    return jsonify({"question": question, "answer": answer})

# 测试用
@app.route("/ask", methods=['GET'])
def ask_get():
    question = request.args.get('question')
    answer = answer_question(question)
    return jsonify({"question": question, "answer": answer})
if __name__ == '__main__':
    print("启动 QA 服务...")
    app.run(host='localhost', port=7050, threaded=True)