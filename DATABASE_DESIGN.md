# 数据库设计文档

## 概述

本文档详细描述无人健身房管理系统的数据库设计方案，包括表结构、字段说明、索引设计和数据关系。

## 数据库信息

- **数据库名称**: `unmanned_gym`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **存储引擎**: InnoDB

## 表结构总览

系统共包含 10 张核心业务表：

| 序号 | 表名 | 中文名 | 记录数估算 | 说明 |
|------|------|--------|-----------|------|
| 1 | user | 用户表 | 1,000 - 10,000 | 存储健身房会员基本信息 |
| 2 | manager | 管理员表 | 5 - 20 | 存储系统管理员账号 |
| 3 | coach | 教练表 | 10 - 50 | 存储健身教练信息 |
| 4 | package | 套餐表 | 5 - 20 | 存储会员卡套餐配置 |
| 5 | order | 订单表 | 10,000 - 100,000 | 存储用户购买订单 |
| 6 | equipment | 器材表 | 20 - 100 | 存储健身器材信息 |
| 7 | time_slot | 时间段表 | 10 - 20 | 存储预约时间段配置 |
| 8 | equipment_booking | 器材预约表 | 5,000 - 50,000 | 存储器材预约记录 |
| 9 | coach_booking | 教练预约表 | 2,000 - 20,000 | 存储教练预约记录 |
| 10 | action_record | 出入记录表 | 50,000 - 500,000 | 存储用户进出记录 |

## 详细表结构

### 1. user - 用户表

**用途**: 存储健身房用户的基本信息和会员状态

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | - | 用户名（登录账号） |
| password | VARCHAR(255) | NOT NULL | - | 密码（BCrypt加密） |
| email | VARCHAR(100) | UNIQUE | NULL | 邮箱地址 |
| user_pic | VARCHAR(500) | - | NULL | 用户头像URL（OSS存储） |
| in_place | TINYINT(1) | - | 0 | 是否在馆内：0-不在，1-在馆 |
| membership_end_time | DATE | - | NULL | 会员结束时间 |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记：y-已删除，n-未删除 |
| status | INT | - | 1 | 账号状态：0-禁用，1-正常 |

**索引设计**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_username` (username)
- UNIQUE KEY: `uk_email` (email)
- INDEX: `idx_membership_end_time` (membership_end_time)

**业务规则**:
- 用户名和邮箱必须唯一
- 密码使用 BCrypt 加密存储
- `in_place` 字段由人脸识别服务自动更新
- `membership_end_time` 根据有效订单自动计算

---

### 2. manager - 管理员表

**用途**: 存储系统管理员账号信息

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 管理员ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | - | 管理员用户名 |
| password | VARCHAR(255) | NOT NULL | - | 密码（BCrypt加密） |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 账号状态：0-禁用，1-正常 |

**索引设计**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_username` (username)

**默认数据**:
```sql
INSERT INTO `manager` (`username`, `password`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');
-- 密码: admin123（需BCrypt加密）
```

---

### 3. coach - 教练表

**用途**: 存储健身教练的个人信息

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 教练ID |
| name | VARCHAR(50) | NOT NULL | - | 教练姓名 |
| email | VARCHAR(100) | UNIQUE | NULL | 邮箱地址 |
| user_pic | VARCHAR(500) | - | NULL | 教练头像URL |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-离职，1-在职 |

**索引设计**:
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_email` (email)

---

### 4. package - 套餐表

**用途**: 存储会员卡套餐的配置信息

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 套餐ID |
| name | VARCHAR(100) | NOT NULL | - | 套餐名称（如：月卡、季卡） |
| price | DECIMAL(10,2) | NOT NULL | 0.00 | 价格（元） |
| level | INT | NOT NULL | 1 | 会员等级（数字越大权限越高） |
| duration | INT | NOT NULL | - | 有效期（天） |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-下架，1-上架 |

**索引设计**:
- PRIMARY KEY: `id`

**示例数据**:
```sql
INSERT INTO `package` (`name`, `price`, `level`, `duration`) VALUES 
('月卡', 299.00, 1, 30),
('季卡', 799.00, 2, 90),
('年卡', 2599.00, 3, 365),
('VIP年卡', 4999.00, 4, 365);
```

---

### 5. order - 订单表

**用途**: 存储用户购买会员套餐的订单记录

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 订单ID |
| uid | INT | NOT NULL, FOREIGN KEY | - | 用户ID（关联user表） |
| pid | INT | NOT NULL, FOREIGN KEY | - | 套餐ID（关联package表） |
| order_start_time | DATE | NOT NULL | - | 订单开始时间 |
| order_end_time | DATE | NOT NULL | - | 订单结束时间 |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-取消，1-有效，2-已完成 |

**索引设计**:
- PRIMARY KEY: `id`
- INDEX: `idx_uid` (uid)
- INDEX: `idx_pid` (pid)
- INDEX: `idx_order_time` (order_start_time, order_end_time)

**外键约束**:
- `fk_order_user`: uid → user.id (ON DELETE CASCADE)
- `fk_order_package`: pid → package.id (ON DELETE CASCADE)

**业务规则**:
- 订单创建时自动更新用户的 `membership_end_time`
- 订单取消时不删除记录，仅修改 status

---

### 6. equipment - 器材表

**用途**: 存储健身房器材的基本信息和可用状态

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 器材ID |
| name | VARCHAR(100) | NOT NULL | - | 器材名称 |
| num | INT | NOT NULL | 0 | 器材总数 |
| spare | INT | NOT NULL | 0 | 当前可用数量 |
| need_coach | TINYINT(1) | - | 0 | 是否需要教练指导：0-否，1-是 |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-维护中，1-可用 |

**索引设计**:
- PRIMARY KEY: `id`

**示例数据**:
```sql
INSERT INTO `equipment` (`name`, `num`, `spare`, `need_coach`) VALUES 
('跑步机', 10, 10, 0),
('动感单车', 8, 8, 0),
('椭圆机', 6, 6, 0),
('卧推架', 4, 4, 1),
('深蹲架', 3, 3, 1),
('哑铃套装', 5, 5, 0),
('划船机', 4, 4, 0),
('综合训练器', 2, 2, 1);
```

**业务规则**:
- `spare` 字段由触发器自动维护（预约时减少，取消时恢复）
- `spare` 不能小于 0，不能大于 `num`

---

### 7. time_slot - 时间段表

**用途**: 存储预约系统的可预约时间段配置

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 时间段ID |
| slot_name | VARCHAR(50) | NOT NULL | - | 时间段名称（如：08:00-09:00） |
| start_time | TIME | NOT NULL | - | 开始时间 |
| end_time | TIME | NOT NULL | - | 结束时间 |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-禁用，1-启用 |

**索引设计**:
- PRIMARY KEY: `id`

**示例数据**:
```sql
INSERT INTO `time_slot` (`slot_name`, `start_time`, `end_time`) VALUES 
('08:00-09:00', '08:00:00', '09:00:00'),
('09:00-10:00', '09:00:00', '10:00:00'),
('10:00-11:00', '10:00:00', '11:00:00'),
('11:00-12:00', '11:00:00', '12:00:00'),
('14:00-15:00', '14:00:00', '15:00:00'),
('15:00-16:00', '15:00:00', '16:00:00'),
('16:00-17:00', '16:00:00', '17:00:00'),
('17:00-18:00', '17:00:00', '18:00:00'),
('18:00-19:00', '18:00:00', '19:00:00'),
('19:00-20:00', '19:00:00', '20:00:00'),
('20:00-21:00', '20:00:00', '21:00:00');
```

---

### 8. equipment_booking - 器材预约表

**用途**: 存储用户对健身器材的预约记录

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 预约ID |
| user_id | INT | NOT NULL, FOREIGN KEY | - | 用户ID |
| equipment_id | INT | NOT NULL, FOREIGN KEY | - | 器材ID |
| booking_date | DATE | NOT NULL | - | 预约日期 |
| slot_id | INT | NOT NULL, FOREIGN KEY | - | 时间段ID |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-取消，1-已预约，2-已完成 |

**索引设计**:
- PRIMARY KEY: `id`
- INDEX: `idx_user_id` (user_id)
- INDEX: `idx_equipment_id` (equipment_id)
- INDEX: `idx_booking_date` (booking_date)
- INDEX: `idx_slot_id` (slot_id)
- UNIQUE KEY: `uk_equipment_date_slot` (equipment_id, booking_date, slot_id)

**外键约束**:
- `fk_equip_booking_user`: user_id → user.id (ON DELETE CASCADE)
- `fk_equip_booking_equipment`: equipment_id → equipment.id (ON DELETE CASCADE)
- `fk_equip_booking_slot`: slot_id → time_slot.id (ON DELETE CASCADE)

**业务规则**:
- 同一器材在同一时间段只能被预约一次（唯一约束保证）
- 预约成功时触发器自动减少器材的 `spare` 数量
- 取消预约时触发器自动恢复器材的 `spare` 数量

---

### 9. coach_booking - 教练预约表

**用途**: 存储用户对教练的预约记录

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 预约ID |
| user_id | INT | NOT NULL, FOREIGN KEY | - | 用户ID |
| coach_id | INT | NOT NULL, FOREIGN KEY | - | 教练ID |
| booking_date | DATE | NOT NULL | - | 预约日期 |
| slot_id | INT | NOT NULL, FOREIGN KEY | - | 时间段ID |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-取消，1-已预约，2-已完成 |

**索引设计**:
- PRIMARY KEY: `id`
- INDEX: `idx_user_id` (user_id)
- INDEX: `idx_coach_id` (coach_id)
- INDEX: `idx_booking_date` (booking_date)
- INDEX: `idx_slot_id` (slot_id)
- UNIQUE KEY: `uk_coach_date_slot` (coach_id, booking_date, slot_id)

**外键约束**:
- `fk_coach_booking_user`: user_id → user.id (ON DELETE CASCADE)
- `fk_coach_booking_coach`: coach_id → coach.id (ON DELETE CASCADE)
- `fk_coach_booking_slot`: slot_id → time_slot.id (ON DELETE CASCADE)

**业务规则**:
- 同一教练在同一时间段只能被一个用户预约（唯一约束保证）
- 不支持自动数量管理（与器材预约不同）

---

### 10. action_record - 出入记录表

**用途**: 存储用户进出健身房的签到签退记录

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | - | 记录ID |
| user_id | INT | NOT NULL, FOREIGN KEY | - | 用户ID |
| arrival_time | DATETIME | - | NULL | 到达时间（签到） |
| departure_time | DATETIME | - | NULL | 离开时间（签退） |
| created_by | VARCHAR(50) | - | 'system' | 创建人 |
| created_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| updated_by | VARCHAR(50) | - | NULL | 更新人 |
| updated_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| is_deleted | CHAR(1) | - | 'n' | 逻辑删除标记 |
| status | INT | - | 1 | 状态：0-异常，1-正常 |

**索引设计**:
- PRIMARY KEY: `id`
- INDEX: `idx_user_id` (user_id)
- INDEX: `idx_arrival_time` (arrival_time)
- INDEX: `idx_departure_time` (departure_time)

**外键约束**:
- `fk_action_record_user`: user_id → user.id (ON DELETE CASCADE)

**业务规则**:
- 用户进入时创建记录，设置 `arrival_time`
- 用户离开时更新记录，设置 `departure_time`
- 同时更新 `user` 表的 `in_place` 字段
- 可通过此表统计用户在馆时长

---

## 视图设计

### 1. v_user_membership - 用户会员状态视图

**用途**: 快速查询用户的会员状态和剩余天数

```sql
CREATE OR REPLACE VIEW `v_user_membership` AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.user_pic,
    u.in_place,
    u.membership_end_time,
    CASE 
        WHEN u.membership_end_time >= CURDATE() THEN 'active'
        ELSE 'expired'
    END AS membership_status,
    DATEDIFF(u.membership_end_time, CURDATE()) AS remaining_days
FROM `user` u
WHERE u.is_deleted = 'n';
```

**使用示例**:
```sql
-- 查询所有有效会员
SELECT * FROM v_user_membership WHERE membership_status = 'active';

-- 查询即将过期的会员（7天内）
SELECT * FROM v_user_membership 
WHERE remaining_days BETWEEN 0 AND 7;
```

---

### 2. v_equipment_availability - 设备可用性视图

**用途**: 查看器材的使用情况和可用率

```sql
CREATE OR REPLACE VIEW `v_equipment_availability` AS
SELECT 
    e.id,
    e.name,
    e.num,
    e.spare,
    e.need_coach,
    (e.num - e.spare) AS in_use,
    ROUND((e.spare / e.num) * 100, 2) AS availability_rate
FROM `equipment` e
WHERE e.is_deleted = 'n' AND e.status = 1;
```

**使用示例**:
```sql
-- 查询可用率低于50%的器材
SELECT * FROM v_equipment_availability WHERE availability_rate < 50;

-- 查询正在使用的器材数量
SELECT SUM(in_use) AS total_in_use FROM v_equipment_availability;
```

---

## 触发器设计

### 1. trg_equipment_booking_after_insert - 器材预约插入触发器

**用途**: 预约成功后自动减少器材可用数量

```sql
DELIMITER $$
CREATE TRIGGER `trg_equipment_booking_after_insert`
AFTER INSERT ON `equipment_booking`
FOR EACH ROW
BEGIN
    IF NEW.status = 1 THEN
        UPDATE `equipment` 
        SET `spare` = `spare` - 1 
        WHERE `id` = NEW.equipment_id;
    END IF;
END$$
DELIMITER ;
```

---

### 2. trg_equipment_booking_after_update - 器材预约更新触发器

**用途**: 预约状态变更时自动调整器材可用数量

```sql
DELIMITER $$
CREATE TRIGGER `trg_equipment_booking_after_update`
AFTER UPDATE ON `equipment_booking`
FOR EACH ROW
BEGIN
    -- 如果从已预约变为取消，恢复数量
    IF OLD.status = 1 AND NEW.status = 0 THEN
        UPDATE `equipment` 
        SET `spare` = `spare` + 1 
        WHERE `id` = NEW.equipment_id;
    -- 如果从取消变为已预约，减少数量
    ELSEIF OLD.status = 0 AND NEW.status = 1 THEN
        UPDATE `equipment` 
        SET `spare` = `spare` - 1 
        WHERE `id` = NEW.equipment_id;
    END IF;
END$$
DELIMITER ;
```

---

## 数据关系图

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    user      │       │   manager    │       │    coach     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)      │
│ username     │       │ username     │       │ name         │
│ password     │       │ password     │       │ email        │
│ email        │       └──────────────┘       │ user_pic     │
│ user_pic     │                              └──────────────┘
│ in_place     │                                     ▲
│ membership_  │                                     │
│   end_time   │                              ┌──────┴──────┐
└───────┬──────┘                              │coach_booking│
        │                                     ├─────────────┤
        │                                     │ id (PK)     │
        │         ┌──────────────┐            │ user_id(FK) │
        │         │action_record │            │ coach_id(FK)│
        │         ├──────────────┤            │booking_date │
        │         │ id (PK)      │            │ slot_id(FK) │
        │         │ user_id(FK)  │            └─────────────┘
        │         │arrival_time  │
        │         │departure_time│                    ▲
        └─────────┴──────────────┘                    │
                                                      │
┌──────────────┐       ┌──────────────┐              │
│   package    │       │    order     │              │
├──────────────┤       ├──────────────┤              │
│ id (PK)      │◄──────│ id (PK)      │              │
│ name         │       │ uid (FK)     │              │
│ price        │       │ pid (FK)     │              │
│ level        │       │order_start   │              │
│ duration     │       │order_end     │              │
└──────────────┘       └──────────────┘              │
                                                      │
┌──────────────┐       ┌──────────────┐              │
│  equipment   │       │time_slot     │              │
├──────────────┤       ├──────────────┤              │
│ id (PK)      │       │ id (PK)      │              │
│ name         │       │ slot_name    │              │
│ num          │       │ start_time   │              │
│ spare        │       │ end_time     │              │
│ need_coach   │       └──────┬───────┘              │
└───────┬──────┘              │                      │
        │                     │                      │
        │         ┌───────────┴───────────┐          │
        │         │ equipment_booking     │          │
        │         ├───────────────────────┤          │
        └────────►│ id (PK)               │          │
                  │ user_id (FK) ─────────┼──────────┘
                  │ equipment_id(FK)      │
                  │ booking_date          │
                  │ slot_id (FK)          │
                  └───────────────────────┘
```

---

## 性能优化建议

### 1. 索引优化

- **高频查询字段**已添加索引：user_id, booking_date, equipment_id
- **组合索引**用于多条件查询：(equipment_id, booking_date, slot_id)
- **唯一索引**保证数据完整性：username, email

### 2. 查询优化

- 使用视图简化复杂查询
- 避免 SELECT *，只查询需要的字段
- 使用 LIMIT 分页查询大数据量表

### 3. 定期维护

```sql
-- 分析表统计信息
ANALYZE TABLE user, order, action_record;

-- 优化表碎片
OPTIMIZE TABLE action_record;

-- 清理过期数据（建议定时任务执行）
DELETE FROM action_record 
WHERE departure_time < DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
AND is_deleted = 'n';
```

### 4. 分区策略（可选）

对于大数据量表（如 action_record），可以考虑按时间分区：

```sql
ALTER TABLE action_record 
PARTITION BY RANGE (YEAR(arrival_time)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

---

## 数据安全

### 1. 敏感数据保护

- **密码**: 使用 BCrypt 加密存储
- **个人信息**: 通过权限控制访问
- **日志审计**: 所有表包含 created_by, updated_by 字段

### 2. 备份策略

```bash
# 每日全量备份
mysqldump -u root -p unmanned_gym > backup_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p unmanned_gym < backup_20240101.sql
```

### 3. 权限管理

```sql
-- 创建应用专用账号（最小权限原则）
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON unmanned_gym.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 常见问题

### Q1: 如何查询某个用户的所有预约记录？

```sql
SELECT 
    eb.id,
    e.name AS equipment_name,
    ts.slot_name,
    eb.booking_date,
    eb.status
FROM equipment_booking eb
JOIN equipment e ON eb.equipment_id = e.id
JOIN time_slot ts ON eb.slot_id = ts.id
WHERE eb.user_id = 1
ORDER BY eb.booking_date DESC;
```

### Q2: 如何统计每天的入馆人数？

```sql
SELECT 
    DATE(arrival_time) AS date,
    COUNT(DISTINCT user_id) AS visitor_count
FROM action_record
WHERE arrival_time >= '2024-01-01'
GROUP BY DATE(arrival_time)
ORDER BY date;
```

### Q3: 如何查询当前在馆内的用户？

```sql
SELECT u.*
FROM user u
WHERE u.in_place = 1
AND u.is_deleted = 'n';
```

### Q4: 如何检查器材的预约冲突？

```sql
-- 唯一约束 uk_equipment_date_slot 已防止冲突
-- 如需查询某器材在某天的预约情况：
SELECT 
    ts.slot_name,
    u.username,
    eb.status
FROM equipment_booking eb
JOIN time_slot ts ON eb.slot_id = ts.id
JOIN user u ON eb.user_id = u.id
WHERE eb.equipment_id = 1
AND eb.booking_date = '2024-01-15'
ORDER BY ts.start_time;
```

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2024-01-01 | 初始版本，包含10张核心表 |
| 1.1 | 2024-01-15 | 添加 membership_end_time 字段到 user 表 |
| 1.2 | 2024-02-01 | 添加视图和触发器 |

---

## 联系方式

如有数据库相关问题，请联系开发团队或提交 Issue。
