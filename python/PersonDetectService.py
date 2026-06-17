import time
from pathlib import Path

from flask import Flask, Response
import cv2
from ultralytics import YOLO
import yaml

app = Flask(__name__)
# CONFIG_PATH = Path(__file__).with_name("config.yaml")
# with open(CONFIG_PATH, encoding="utf-8") as f:
#     cfg = yaml.safe_load(f)
# user = cfg["person_detect"]["user"]
# pwd = cfg["person_detect"]["pwd"]
# ip = cfg["person_detect"]["ip"]
# port = cfg["person_detect"]["port"]
# rtsp_url = f"rtsp://{user}:{pwd}@{ip}:{port}/live"
# print(rtsp_url)
cap = cv2.VideoCapture(1, cv2.CAP_DSHOW)  # 0 代表默认摄像头
# cap.set(cv2.CAP_PROP_OPEN_TIMEOUT_MSEC, 5_000)   # 5 秒
# cap.set(cv2.CAP_PROP_READ_TIMEOUT_MSEC,  5_000)
model = YOLO("yolov5n.pt")
person_count = 0


def detect(frame):
    # 1. 推理
    results = model.predict(frame, conf=0.8, device="cpu",verbose=False)

    # 2. 只留 person（COCO 类别 0）
    persons = [d for d in results[0].boxes.data if int(d[5]) == 0]
    global person_count
    person_count = len(persons)

    # 3. 画框 + 置信度
    for d in persons:
        x1, y1, x2, y2, conf, cls = d.tolist()
        cv2.rectangle(frame, (int(x1), int(y1)),
                      (int(x2), int(y2)), (0, 255, 0), 2)
        cv2.putText(frame, f"{conf:.2f}",
                    (int(x1), int(y1) - 5),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6,
                    (0, 255, 0), 2)

    # 4. 写人数
    cv2.putText(frame, f"Person: {person_count}",
                (10, 30), cv2.FONT_HERSHEY_SIMPLEX,
                1, (0, 0, 255), 2)

    return frame, person_count


def gen():
    global cap
    cnt = 0

    # 最近一次“处理完”的 jpeg 字节
    while not cap.isOpened():
        print("摄像头正在启动")
        time.sleep(1)
    while True:
        cnt += 1
        cache = None
        ret, frame = cap.read()
        if not ret:
            cap.release()
            time.sleep(2)
            cap = cv2.VideoCapture(1, cv2.CAP_DSHOW)
            continue

        # 1. 只有第 1、4、7... 帧才做 AI
        # cv2.imshow("win", frame)
        if cnt % 3 == 1:
            frame, person_count = detect(frame)
            # cv2.imshow("win", frame)
            cv2.putText(frame, f"Person: {person_count}", (10, 60),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
            _, jpeg = cv2.imencode('.jpg', frame, [cv2.IMWRITE_JPEG_QUALITY, 75])
            cache = jpeg  # 更新缓存

        # 2. 其余帧直接跳过（浏览器保持旧图，带宽降为 1/3）
        if cache is None:
            continue
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' +
               cache.tobytes() +     # ← 先转 bytes
               b'\r\n')


@app.get('/py/person_count')
def get_count():
    return {"person_count": person_count}


@app.route('/py/video')
def video():
    # multipart/x-mixed-replace 是 MJPEG 标准 MIME
    return Response(gen(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000, threaded=True)

