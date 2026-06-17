# face_service.py
import pickle, os, cv2, numpy as np, faiss
import threading
import time
from queue import Queue, Empty
import requests
import yaml
from PIL import Image, ImageDraw, ImageFont
from deepface import DeepFace
from pathlib import Path
from flask import Flask, request, jsonify

app = Flask(__name__)

CONFIG_PATH = Path(__file__).with_name("config.yaml")

with open(CONFIG_PATH, encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

os.environ["DEEPFACE_HOME"] = cfg["face_data"]["weight_path"]

index = None
labels = None

name_queue = Queue(maxsize=1)
msg_queue = Queue(maxsize=1)


# ---------- 2. 工具函数 ----------
def get_emb(img):
    faces = DeepFace.extract_faces(img, detector_backend="yunet", enforce_detection=False)
    if not faces:
        print("no face")
        raise RuntimeError("no face")
    face = faces[0]["face"]  # 0~1 float
    face = cv2.resize(face, (160, 160))  # 尺寸对齐
    face = (face * 255).astype(np.uint8)  # 像素范围对齐
    emb = DeepFace.represent(face, model_name="Facenet", detector_backend="skip")[0]["embedding"]
    return emb


# ---------- 3. 查询 ----------
def query(frame):
    # q = frame.reshape(1, -1)
    q = np.array(get_emb(frame), dtype=np.float32).reshape(1, -1)
    faiss.normalize_L2(q)
    D, I = index.search(q, 1)
    score = 1 - D[0, 0]
    id = labels[I[0, 0]]
    return id, float(score)


# Java 接收推送的地址（可配环境变量）
JAVA_PUSH_URL = os.getenv("JAVA_PUSH_URL", "http://localhost:80/api/face/check-in")


def async_push(uid):
    try:
        print(f"[DEBUG] 准备推送人脸数据: uid={uid}, URL={JAVA_PUSH_URL}")
        # 增加超时时间到10秒，给Java后端足够的处理时间
        resp = requests.post(JAVA_PUSH_URL,
                             json={"code": 200, "msg": "success", "data": uid},
                             timeout=10)
        print(f"[DEBUG] HTTP状态码: {resp.status_code}")
        print(f"[DEBUG] 响应内容: {resp.text}")

        result = resp.json()
        real_name = result.get('data')
        msg=result.get("msg")
        name_queue.put(real_name)
        msg_queue.put(msg)
        print(f"[DEBUG] 推送成功: name={real_name}, msg={msg}")
    except Exception as e:
        print("[ERROR] push error:", e)
        import traceback
        traceback.print_exc()


def background_loop(cam_url, interval):
    push_gap = 3
    last_name_time = 0  # 姓名开始显示的时刻
    display_duration = 2  # 秒，想留多久就设多久
    current_name = ""  # 当前正在显示的名字
    current_msg=""
    last_push = time.time()

    for try_open in range(5):
        cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
        if cap.isOpened():
            ret, _ = cap.read()  # 空读稳定
            if ret:
                print(f"[BG] 摄像头 {cam_url} 打开成功")
                break
        cap.release()
    else:
        print("[BG] 打开失败，线程退出")
        return

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        uid, score = query(frame)
        now = time.time()

        # 1. 识别成功且冷却到了，更新名字并刷新保质期
        if score > 0.4 and now - last_push >= push_gap:
            last_push = now
            threading.Thread(target=async_push, args=(uid), daemon=True).start()

            # 把后端返回的姓名取回来（这里简写，实际仍用 queue/全局变量）
            try:
                current_name = name_queue.get_nowait()
                current_msg=msg_queue.get_nowait()
            except Empty:
                current_name = ""
            last_name_time = now  # 刷新保质期

        # 2. 保质期过了就清空名字
        if now - last_name_time > display_duration:
            current_name = ""
            current_msg=""

        # 3. 画图
        pil_img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        font = ImageFont.truetype("simsun.ttc", 28)
        draw = ImageDraw.Draw(pil_img)
        if current_name:  # 有名字才画
            draw.text((10, 30), f"姓名：{current_name}  {current_msg}", font=font, fill=(0, 255, 0))
        else:  # 否则画“未识别”或干脆不画
            draw.text((10, 30), "", font=font, fill=(0, 0, 255))
        frame = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

        cv2.imshow("win", frame)
        if cv2.waitKey(1) == 27:
            break


@app.post("/py/face/start")
def start():
    """Java 调这个接口启动后台检测"""
    cam_url = 0
    interval = 400  # ms
    global index
    global labels
    print("Loading bank …")
    with open("bank.pk", "rb") as f:
        index, labels = pickle.load(f)
    print("向量总数:", index.ntotal)
    # 启动后台线程（Daemon 不会阻塞 Flask）
    thread = threading.Thread(target=background_loop, args=(cam_url, interval), daemon=True)
    thread.start()
    # background_loop(cam_url,interval)
    return jsonify({"code": 200, "msg": "后台检测已启动"})


@app.post("/py/face/stop")
def stop():
    # 简单方案：全局标志位 / 或线程池管理
    # 这里演示用「停止 Flask」即可，生产用线程池 + 事件
    return jsonify({"code": 200, "msg": "请重启服务以停止"})


if __name__ == '__main__':
    app.run(host='localhost', port=5000, threaded=True)
