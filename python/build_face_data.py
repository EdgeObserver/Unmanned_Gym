# 1. 建库（只做一次）
import pickle, os, cv2, numpy as np
from deepface import DeepFace
import faiss
from pathlib import Path
from collections import defaultdict
# face_service.py
from flask import Flask, request, jsonify

app = Flask(__name__)

import yaml

CONFIG_PATH = Path(__file__).with_name("config.yaml")
with open(CONFIG_PATH, encoding="utf-8") as f:
    cfg = yaml.safe_load(f)

os.environ["DEEPFACE_HOME"] = cfg["face_data"]["weight_path"]


def get_emb(img_path):
    img = cv2.imread(str(img_path))
    if img is None:
        raise RuntimeError("cv2 无法读取 " + str(img_path))
    faces = DeepFace.extract_faces(img, detector_backend="yunet", enforce_detection=False)
    if len(faces) == 0:
        raise RuntimeError("未检测到人脸 " + str(img_path))
    face_rgb = faces[0]["face"]
    return DeepFace.represent(face_rgb, model_name="Facenet", detector_backend="skip")[0]["embedding"]


def do_build(db_dir):
    # 2. 按“文件名前缀”聚类（一人多图）

    db_dir = Path(db_dir)

    exts = ("*.jpg", "*.jpeg", "*.png")

    img_paths = [p for ext in exts for p in db_dir.glob(ext)]

    group = defaultdict(list)  # 前缀 -> [路径1, 路径2, ...]
    for p in img_paths:
        prefix = p.stem.split('_')[0]  # zhangsan_0001.jpg -> zhangsan
        group[prefix].append(p)

    # 3. 建库：每人平均向量
    bank, labels = [], []
    for prefix, paths in group.items():
        embs = []
        for p in paths:
            try:
                embs.append(get_emb(p))
            except Exception as e:
                print("跳过", p, e)
        if embs:  # 必须采到图
            center = np.mean(embs, axis=0)
            bank.append(center)
            labels.append(prefix)
            print(prefix, "样本数：", len(embs))

    bank = np.array(bank, dtype=np.float32)  # (n_person, 128)
    faiss.normalize_L2(bank)
    index = faiss.IndexFlatIP(bank.shape[1])
    index.add(bank)

    # 落盘
    with open("bank.pk", "wb") as f:
        pickle.dump((index, labels), f)
    print("建库完成，身份数：", len(labels))
    # return os.path.abspath()+"\\bank.pk"


@app.post("/py/face/build")
def build():
    try:
        db_dir = cfg["face_data"]["db_dir"]
        print(db_dir)
        do_build(db_dir)
        print("Loading bank …")
        with open("bank.pk", "rb") as f:
            index, labels = pickle.load(f)
            return jsonify({"msg": "success"})
    except Exception as e:
    #     print("fail")
        return jsonify({"msg": "fail"})
    # return jsonify({"msg":"test"})


if __name__ == "__main__":
    app.run(host="localhost", port=5050)
