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
from collections import defaultdict
import sys,io
# 初始化 Flask 应用
app = Flask(__name__)

CONFIG_PATH = Path(__file__).with_name("config.yaml")
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

with open(CONFIG_PATH, encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

os.environ["DEEPFACE_HOME"] = cfg["face_data"]["weight_path"]

# 全局变量
index = None
labels = None
recognition_thread = None
is_running = False

name_queue = Queue(maxsize=1)
msg_queue = Queue(maxsize=1)


# ---------- 1. 建库功能 ----------
def get_emb(img_path=None, img_array=None):
    """
    提取人脸特征向量
    :param img_path: 图片路径
    :param img_array: 直接传入的图片数组
    :return: 特征向量
    """
    if img_path:
        img = cv2.imread(str(img_path))
        if img is None:
            raise RuntimeError("cv2 无法读取 " + str(img_path))
    elif img_array is not None:
        img = img_array
    else:
        raise ValueError("必须提供 img_path 或 img_array 参数")

    faces = DeepFace.extract_faces(img, detector_backend="yunet", enforce_detection=False)
    if len(faces) == 0:
        raise RuntimeError("未检测到人脸")

    face_rgb = faces[0]["face"]
    face = cv2.resize(face_rgb, (160, 160))  # 尺寸对齐
    face = (face * 255).astype(np.uint8)  # 像素范围对齐
    emb = DeepFace.represent(face, model_name="Facenet", detector_backend="skip")[0]["embedding"]
    return emb


def do_build(db_dir):
    """
    构建人脸特征库
    :param db_dir: 包含人脸图片的目录
    """
    # 2. 按"文件名前缀"聚类（一人多图）
    db_dir = Path(db_dir)

    exts = ("*.jpg", "*.jpeg", "*.png")

    img_paths = [p for ext in exts for p in db_dir.glob(ext)]

    group = defaultdict(list)  # 前缀 -> [路径1, 路径2, ...]
    for p in img_paths:
        prefix = p.stem.split('_')[0]  # zhangsan_0001.jpg -> zhangsan
        group[prefix].append(p)

    # 3. 建库：每人平均向量
    bank, labels_list = [], []
    for prefix, paths in group.items():
        embs = []
        for p in paths:
            try:
                embs.append(get_emb(img_path=p))
            except Exception as e:
                print("跳过", p, e)
        if embs:  # 必须采到图
            center = np.mean(embs, axis=0)
            bank.append(center)
            labels_list.append(prefix)
            print(prefix, "样本数：", len(embs))

    bank = np.array(bank, dtype=np.float32)  # (n_person, 128)
    faiss.normalize_L2(bank)
    index_new = faiss.IndexFlatIP(bank.shape[1])
    index_new.add(bank)
    save_path = cfg["face_data"]["bank_file_path"]
    # 落盘
    with open(save_path, "wb") as f:
        pickle.dump((index_new, labels_list), f)
    print("建库完成，身份数：", len(labels_list))
    return len(labels_list)


# ---------- 2. 人脸识别功能 ----------
def query(frame):
    """
    识别人脸
    :param frame: 输入图像
    :return: 用户ID和相似度分数
    """
    q = np.array(get_emb(img_array=frame), dtype=np.float32).reshape(1, -1)
    faiss.normalize_L2(q)
    D, I = index.search(q, 1)
    score = 1 - D[0, 0]
    user_id = labels[I[0, 0]]
    return user_id, float(score)


# Java 接收推送的地址（可配环境变量）
JAVA_PUSH_URL = cfg["face_detect"]["in_url"]


def async_push(uid):
    try:
        print(f"[DEBUG] 准备推送人脸数据: uid={uid}, URL={JAVA_PUSH_URL}")
        resp = requests.post(JAVA_PUSH_URL,
                             json={"code": 200, "msg": "success", "data": uid},
                             timeout=2)
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
    """
    后台人脸识别循环
    :param cam_url: 摄像头地址
    :param interval: 检测间隔
    """
    global is_running
    push_gap = 3
    last_name_time = 0  # 姓名开始显示的时刻
    display_duration = 2  # 秒，想留多久就设多久
    current_name = ""  # 当前正在显示的名字
    current_msg = ""
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
        is_running = False
        return

    while is_running:
        ret, frame = cap.read()
        if not ret:
            break

        try:
            uid, score = query(frame)
        except Exception as e:
            print("人脸识别失败:", e)
            continue

        now = time.time()
        print(score)
        # 1. 识别成功且冷却到了，更新名字并刷新保质期
        if score > 0.4 and now - last_push >= push_gap:

            last_push = now
            threading.Thread(target=async_push, args=(uid,), daemon=True).start()

            # 把后端返回的姓名取回来
            try:
                current_name = name_queue.get_nowait()
                current_msg = msg_queue.get_nowait()
            except Empty:
                current_name = ""
            last_name_time = now  # 刷新保质期

        # 2. 保质期过了就清空名字
        if now - last_name_time > display_duration:
            current_name = ""
            current_msg = ""

        # 3. 画图
        pil_img = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        font = ImageFont.truetype("simsun.ttc", 28)
        draw = ImageDraw.Draw(pil_img)
        if current_name:  # 有名字才画
            draw.text((10, 30), f"姓名：{current_name}  {current_msg}", font=font, fill=(0, 255, 0))
        else:  # 否则画"未识别"或干脆不画
            draw.text((10, 30), "", font=font, fill=(0, 0, 255))
        frame = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

        cv2.imshow("win", frame)
        if cv2.waitKey(1) == 27:  # ESC键退出
            is_running = False
            break

    cap.release()
    cv2.destroyAllWindows()
    print("人脸识别服务已停止")


# ---------- 3. Flask API 接口 ----------
@app.route('/py/face/build', methods=['POST'])
def build_face_bank():
    """
    构建人脸特征库
    """
    try:
        db_dir = cfg["face_data"]["db_dir"]
        print(f"构建人脸库，目录: {db_dir}")
        nums = do_build(db_dir)
        return jsonify({
            "code": 200,
            "msg": "人脸库构建成功",
            "data": {"nums": nums}
        })
    except Exception as e:
        print(f"构建人脸库失败: {e}")
        return jsonify({
            "code": 500,
            "msg": f"构建失败: {str(e)}",
            "data": {"nums": 0}
        })


@app.route('/py/face/start', methods=['POST'])
def start_face_recognition():
    """
    启动人脸识别服务
    """
    global recognition_thread, is_running, index, labels
    bank_file_path = cfg["face_data"]["bank_file_path"]
    # 检查是否已在运行
    if is_running:
        return jsonify({
            "code": 400,
            "msg": "人脸识别服务已在运行中",
            "data": None
        })

    # 加载人脸库
    print("加载人脸库...")
    try:
        with open(bank_file_path, "rb") as f:
            index, labels = pickle.load(f)
        print(f"向量总数: {index.ntotal}, 身份数: {len(labels)}")
    except FileNotFoundError:
        return jsonify({
            "code": 500,
            "msg": "人脸库文件 bank.pk 不存在，请先构建人脸库",
            "data": None
        })
    except Exception as e:
        return jsonify({
            "code": 500,
            "msg": f"加载人脸库失败: {str(e)}",
            "data": None
        })

    # 启动后台线程
    is_running = True
    cam_url = 0
    interval = 400  # ms
    recognition_thread = threading.Thread(
        target=background_loop,
        args=(cam_url, interval),
        daemon=True
    )
    recognition_thread.start()

    return jsonify({
        "code": 200,
        "msg": "人脸识别服务已启动",
        "data": None
    })


@app.route('/py/face/stop', methods=['POST'])
def stop_face_recognition():
    """
    停止人脸识别服务
    """
    global is_running

    if not is_running:
        return jsonify({
            "code": 400,
            "msg": "人脸识别服务未在运行",
            "data": None
        })

    is_running = False
    return jsonify({
        "code": 200,
        "msg": "人脸识别服务已停止",
        "data": None
    })


@app.route('/py/face/status', methods=['GET'])
def get_status():
    """
    获取人脸识别服务状态
    """
    return jsonify({
        "code": 200,
        "msg": "获取状态成功",
        "data": {"is_running": is_running}
    })


if __name__ == '__main__':
    print("启动 FaceService 服务...")
    app.run(host='localhost', port=5000, threaded=True)
