# qa_engine.py

import re
from neo4j_driver import neo4j_conn
# ====== 同义词词典（可按需扩展） ======
MUSCLE_SYNONYMS = {
    "胸肌": ["胸", "胸部", "大胸", "胸大肌", "练胸", "丰胸"],
    "背阔肌": ["背", "背部", "宽背", "练背", "倒三角"],
    "股四头肌": ["大腿前侧", "大腿", "腿部", "腿前", "腿", "quadriceps"],
    "腘绳肌": ["大腿后侧", "腿后", "hamstring"],
    "三角肌": ["肩膀", "肩部", "肩", " deltoid"],
    "肱二头肌": ["小臂前侧", "手臂前", "弯举肌", "bicep"],
    "肱三头肌": ["手臂后侧", "拜拜肉", " tricep"],
    "核心肌群": ["腹部", "肚子", "腹肌", "core", "腰腹"],
    "臀大肌": ["屁股", "臀部", "翘臀", "glutes", "练臀"],
    "小腿肌群": ["小腿", "腿", "腓肠肌", "calf"],
    "斜方肌": ["上背", "脖子下面", " traps"]
}

EQUIPMENT_SYNONYMS = {
    "自重": ["徒手", "不用器械", "家里练", "bodyweight", "没器械", "空手"],
    "杠铃": ["barbell", "大杠铃", "标准杠铃"],
    "哑铃": ["dumbbell", "小哑铃", "一对哑铃"],
    "弹力带": ["阻力带", "拉力带"],
    "单杠": ["引体向上杆", " pull-up bar"],
    "高位下拉器": ["下拉机", "lat pulldown machine"]
}

DIFFICULTY_SYNONYMS = {
    "初级": ["新手", "简单", "入门", "初学者", "容易", "轻松"],
    "中级": ["一般", "普通", "中等"],
    "高级": ["高手", "困难", "进阶", "挑战", "hard"]
}

# 所有标准值（用于 fallback）
ALL_MUSCLES = list(MUSCLE_SYNONYMS.keys())
ALL_EQUIPMENTS = list(EQUIPMENT_SYNONYMS.keys())
ALL_DIFFICULTIES = list(DIFFICULTY_SYNONYMS.keys())

def _normalize_text(text: str) -> str:
    """标准化文本：去空格、统一术语"""
    text = text.lower().strip()
    text = text.replace("？", "").replace("?", "")
    # 可加更多清洗规则
    return text

def _match_synonyms(text: str, synonym_dict: dict):
    """从同义词字典中匹配第一个命中的标准词"""
    for canonical, synonyms in synonym_dict.items():
        for syn in synonyms:
            if syn in text:
                return canonical
    return None

def _extract_with_patterns(text: str):
    """使用正则模板提取结构化信息"""
    patterns = [
        # 模板1: "用[器械]练[部位]"
        (r"用(?:.*?)([^\s]+?)练([^\s]+)", "use_equip_for_muscle"),
        # 模板2: "[动作]练[部位]?" 或 "[动作]练什么"
        (r"([^\s]+?)练(?:什么|哪(?:块肌肉)?|([^\s]+))?", "exercise_targets"),
        # 模板3: "练[部位]的[难度]动作"
        (r"练([^\s]+?)(?:的|有)?(?:.*?)(初级|中级|高级|新手|简单|进阶|高手)", "muscle_with_difficulty"),
        # 模板4: "[部位]怎么练" / "如何练[部位]"
        (r"(?:怎么|如何|怎样)练([^\s]+)", "how_to_train_muscle")
    ]

    for pattern, intent in patterns:
        match = re.search(pattern, text)
        if match:
            return intent, match.groups()
    return None, None

# ... existing code ...

def parse_question(question: str) -> dict:
    """
    增强版问题解析器
    返回：{
        "intent": "list_exercises" | "exercise_info",
        "muscle": str | None,
        "equipment": str | None,
        "difficulty": str | None,
        "exercise_name": str | None,
        "negation": bool  # 是否包含否定（如"不用杠铃"）
    }
    """
    original = question
    q = _normalize_text(question)

    result = {
        "intent": "list_exercises",
        "muscle": None,
        "equipment": None,
        "difficulty": None,
        "exercise_name": None,
        "negation": False,
        "raw": original
    }

    # === 步骤 1: 检查是否是否定句（如"不用杠铃"）===
    if any(word in q for word in ["不用", "不要", "无需", "没有", "没用"]):
        result["negation"] = True

    # === 步骤 2: 尝试正则模板匹配 ===
    intent_type, groups = _extract_with_patterns(q)

    # 特殊处理：如果是"怎么练"、"如何练"等，不要误判为查询具体动作
    if intent_type == "exercise_targets" and groups[0]:
        # 检查是否是疑问词开头的问题（如"怎么练"、"如何练"）
        question_words = ["怎么", "如何", "怎样", "什么", "哪些", "哪个"]
        is_question_word = any(qw in groups[0] for qw in question_words)

        if not is_question_word:
            # 如"俯卧撑练什么"才是查询具体动作
            result["intent"] = "exercise_info"
            result["exercise_name"] = groups[0]
            return result

    # === 步骤 3: 同义词匹配（覆盖模板未覆盖的情况）===
    # 提取肌群
    muscle = _match_synonyms(q, MUSCLE_SYNONYMS)
    if not muscle:
        # fallback: 直接匹配标准肌群名
        for m in ALL_MUSCLES:
            if m in q:
                muscle = m
                break
    result["muscle"] = muscle

    # 提取器械
    equipment = _match_synonyms(q, EQUIPMENT_SYNONYMS)
    if not equipment:
        for eq in ALL_EQUIPMENTS:
            if eq in q:
                equipment = eq
                break
    result["equipment"] = equipment

    # 提取难度
    difficulty = _match_synonyms(q, DIFFICULTY_SYNONYMS)
    if not difficulty:
        for d in ALL_DIFFICULTIES:
            if d in q:
                difficulty = d
                break
    result["difficulty"] = difficulty

    # === 步骤 4: 处理否定逻辑（如"不用杠铃" → 排除杠铃）===
    if result["negation"] and result["equipment"]:
        # 标记：后续查询需排除该器械
        # （在生成 Cypher 时处理）
        pass  # 这里只标记，Cypher 生成时再处理

    # === 步骤 5: 如果问的是具体动作（且未被模板捕获）===
    # 常见动作列表，只有这些才被认为是查询具体动作
    common_exercises = [
        "俯卧撑", "深蹲", "硬拉", "引体向上", "卧推", "划船", "推举",
        "弯举", "臂屈伸", "腿举", "腿弯举", "提踵", "仰卧起坐", "平板支撑"
    ]

    if not result["exercise_name"]:
        for ex in common_exercises:
            if ex in original:  # 用原始文本避免大小写问题
                # 确保不是疑问句式（如"俯卧撑怎么练"是询问方法，不是查询信息）
                if not any(qw in q for qw in ["怎么", "如何", "怎样"]):
                    result["intent"] = "exercise_info"
                    result["exercise_name"] = ex
                    break

    return result

# ... existing code ...


# qa_engine.py（接在 parse_question 后面）

def answer_question(question: str) -> str:
    """
    主问答函数：接收自然语言问题，返回自然语言答案
    """
    parsed = parse_question(question)

    # === 情况1：查询具体动作的信息（如“俯卧撑练什么？”）===
    if parsed["intent"] == "exercise_info" and parsed["exercise_name"]:
        query = """
        MATCH (e:Exercise {name: $name})
        OPTIONAL MATCH (e)-[:TARGETS]->(m:MuscleGroup)
        OPTIONAL MATCH (e)-[:USES]->(eq:Equipment)
        WITH e, collect(DISTINCT m.name) AS muscles, eq.name AS equipment
        RETURN e.name AS name, e.difficulty AS difficulty, 
               e.description AS desc, muscles, equipment
        """
        results = neo4j_conn.query(query, {"name": parsed["exercise_name"]})

        if not results or not results[0]["name"]:
            return f"抱歉，知识库中没有找到动作「{parsed['exercise_name']}」。"

        r = results[0]
        muscles_str = "、".join(r["muscles"]) if r["muscles"] else "未知"
        equipment_str = r["equipment"] if r["equipment"] else "未知"

        return (
            f"「{r['name']}」\n"
            f"- 难度：{r['difficulty']}\n"
            f"- 目标肌群：{muscles_str}\n"
            f"- 所需器械：{equipment_str}\n"
            f"- 说明：{r['desc']}"
        )

    # === 情况2：列出符合条件的动作 ===
    where_clauses = []
    params = {}

    # 肌群条件
    if parsed["muscle"]:
        where_clauses.append("(e)-[:TARGETS]->(:MuscleGroup {name: $muscle})")
        params["muscle"] = parsed["muscle"]

    # 器械条件（普通）
    if parsed["equipment"] and not parsed["negation"]:
        where_clauses.append("(e)-[:USES]->(:Equipment {name: $equipment})")
        params["equipment"] = parsed["equipment"]

    # 器械否定条件（如“不用杠铃”）
    if parsed["negation"] and parsed["equipment"]:
        where_clauses.append("NOT (e)-[:USES]->(:Equipment {name: $exclude_equipment})")
        params["exclude_equipment"] = parsed["equipment"]

    # 难度条件
    if parsed["difficulty"]:
        where_clauses.append("e.difficulty = $difficulty")
        params["difficulty"] = parsed["difficulty"]

    # 如果没有任何条件，提示用户
    if not where_clauses:
        return (
            "请告诉我你想练的部位（如胸、背、腿）、"
            "使用的器械（如哑铃、自重）或难度（初级/中级/高级），\n"
            "例如：\n• “练胸的初级动作”\n• “用哑铃练背”\n• “不用杠铃的腿部训练”"
        )

    # 构建最终查询
    match_clause = "MATCH (e:Exercise)"
    where_clause = "WHERE " + " AND ".join(where_clauses) if where_clauses else ""
    query = f"""
    {match_clause}
    {where_clause}
    RETURN e.name AS name, e.difficulty AS difficulty, e.description AS desc
    ORDER BY e.difficulty
    LIMIT 10
    """

    results = neo4j_conn.query(query, params)

    if not results:
        # 构建友好提示
        parts = []
        if parsed["muscle"]: parts.append(f"目标肌群：{parsed['muscle']}")
        if parsed["equipment"]:
            if parsed["negation"]:
                parts.append(f"排除器械：{parsed['equipment']}")
            else:
                parts.append(f"使用器械：{parsed['equipment']}")
        if parsed["difficulty"]: parts.append(f"难度：{parsed['difficulty']}")
        condition_str = "、".join(parts)
        return f"没有找到满足条件（{condition_str}）的动作。试试放宽条件？"

    # 生成自然语言答案
    answer = f"找到 {len(results)} 个推荐动作：\n"
    for i, r in enumerate(results, 1):
        answer += f"{i}. **{r['name']}**（{r['difficulty']}）\n   {r['desc']}\n"
    return answer