-- ============================================================================
-- Unmanned Gym Database Schema
-- 无人健身房管理系统数据库初始化脚本
-- ============================================================================
-- 数据库: unmanned_gym
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `unmanned_gym` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `unmanned_gym`;

-- ============================================================================
-- 1. 用户表 (user)
-- 描述: 存储健身房用户基本信息
-- ============================================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `user_pic` VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL',
    `in_place` TINYINT(1) DEFAULT 0 COMMENT '是否在馆内: 0-不在, 1-在馆',
    `membership_end_time` DATE DEFAULT NULL COMMENT '会员结束时间',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_membership_end_time` (`membership_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================================
-- 2. 管理员表 (manager)
-- 描述: 存储系统管理员信息
-- ============================================================================
DROP TABLE IF EXISTS `manager`;
CREATE TABLE `manager` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ============================================================================
-- 3. 教练表 (coach)
-- 描述: 存储健身教练信息
-- ============================================================================
DROP TABLE IF EXISTS `coach`;
CREATE TABLE `coach` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '教练ID',
    `name` VARCHAR(50) NOT NULL COMMENT '教练姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `user_pic` VARCHAR(500) DEFAULT NULL COMMENT '教练头像URL',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练表';

-- ============================================================================
-- 4. 套餐表 (package)
-- 描述: 存储会员卡套餐信息
-- ============================================================================
DROP TABLE IF EXISTS `package`;
CREATE TABLE `package` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    `name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    `level` INT NOT NULL DEFAULT 1 COMMENT '会员等级',
    `duration` INT NOT NULL COMMENT '有效期（天）',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='套餐表';

-- ============================================================================
-- 5. 订单表 (order)
-- 描述: 存储用户购买会员套餐的订单信息
-- ============================================================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `uid` INT NOT NULL COMMENT '用户ID',
    `pid` INT NOT NULL COMMENT '套餐ID',
    `order_start_time` DATE NOT NULL COMMENT '订单开始时间',
    `order_end_time` DATE NOT NULL COMMENT '订单结束时间',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-取消, 1-有效, 2-已完成',
    PRIMARY KEY (`id`),
    KEY `idx_uid` (`uid`),
    KEY `idx_pid` (`pid`),
    KEY `idx_order_time` (`order_start_time`, `order_end_time`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`uid`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_package` FOREIGN KEY (`pid`) REFERENCES `package` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================================
-- 6. 器材表 (equipment)
-- 描述: 存储健身房器材信息
-- ============================================================================
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '器材ID',
    `name` VARCHAR(100) NOT NULL COMMENT '器材名称',
    `num` INT NOT NULL DEFAULT 0 COMMENT '器材总数',
    `spare` INT NOT NULL DEFAULT 0 COMMENT '可用数量',
    `need_coach` TINYINT(1) DEFAULT 0 COMMENT '是否需要教练指导: 0-否, 1-是',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-维护中, 1-可用',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='器材表';

-- ============================================================================
-- 7. 时间段表 (time_slot)
-- 描述: 存储预约时间段信息
-- ============================================================================
DROP TABLE IF EXISTS `time_slot`;
CREATE TABLE `time_slot` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
    `slot_name` VARCHAR(50) NOT NULL COMMENT '时间段名称',
    `start_time` TIME NOT NULL COMMENT '开始时间',
    `end_time` TIME NOT NULL COMMENT '结束时间',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间段表';

-- ============================================================================
-- 8. 器材预约表 (equipment_booking)
-- 描述: 存储用户器材预约记录
-- ============================================================================
DROP TABLE IF EXISTS `equipment_booking`;
CREATE TABLE `equipment_booking` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `equipment_id` INT NOT NULL COMMENT '器材ID',
    `booking_date` DATE NOT NULL COMMENT '预约日期',
    `slot_id` INT NOT NULL COMMENT '时间段ID',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-取消, 1-已预约, 2-已完成',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_equipment_id` (`equipment_id`),
    KEY `idx_booking_date` (`booking_date`),
    KEY `idx_slot_id` (`slot_id`),
    UNIQUE KEY `uk_equipment_date_slot` (`equipment_id`, `booking_date`, `slot_id`),
    CONSTRAINT `fk_equip_booking_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_equip_booking_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_equip_booking_slot` FOREIGN KEY (`slot_id`) REFERENCES `time_slot` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='器材预约表';

-- ============================================================================
-- 9. 教练预约表 (coach_booking)
-- 描述: 存储用户教练预约记录
-- ============================================================================
DROP TABLE IF EXISTS `coach_booking`;
CREATE TABLE `coach_booking` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `coach_id` INT NOT NULL COMMENT '教练ID',
    `booking_date` DATE NOT NULL COMMENT '预约日期',
    `slot_id` INT NOT NULL COMMENT '时间段ID',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-取消, 1-已预约, 2-已完成',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coach_id` (`coach_id`),
    KEY `idx_booking_date` (`booking_date`),
    KEY `idx_slot_id` (`slot_id`),
    UNIQUE KEY `uk_coach_date_slot` (`coach_id`, `booking_date`, `slot_id`),
    CONSTRAINT `fk_coach_booking_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_coach_booking_coach` FOREIGN KEY (`coach_id`) REFERENCES `coach` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_coach_booking_slot` FOREIGN KEY (`slot_id`) REFERENCES `time_slot` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练预约表';

-- ============================================================================
-- 10. 出入记录表 (action_record)
-- 描述: 存储用户进出健身房的记录
-- ============================================================================
DROP TABLE IF EXISTS `action_record`;
CREATE TABLE `action_record` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `arrival_time` DATETIME DEFAULT NULL COMMENT '到达时间',
    `departure_time` DATETIME DEFAULT NULL COMMENT '离开时间',
    `created_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` CHAR(1) DEFAULT 'n' COMMENT '是否删除: y-是, n-否',
    `status` INT DEFAULT 1 COMMENT '状态: 0-异常, 1-正常',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_arrival_time` (`arrival_time`),
    KEY `idx_departure_time` (`departure_time`),
    CONSTRAINT `fk_action_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出入记录表';

-- ============================================================================
-- 初始化数据
-- ============================================================================

-- 插入默认管理员账号 (密码: admin123，实际使用时需要加密)
INSERT INTO `manager` (`username`, `password`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');

-- 插入默认时间段
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

-- 插入示例套餐
INSERT INTO `package` (`name`, `price`, `level`, `duration`) VALUES 
('月卡', 299.00, 1, 30),
('季卡', 799.00, 2, 90),
('年卡', 2599.00, 3, 365),
('VIP年卡', 4999.00, 4, 365);

-- 插入示例器材
INSERT INTO `equipment` (`name`, `num`, `spare`, `need_coach`) VALUES 
('跑步机', 10, 10, 0),
('动感单车', 8, 8, 0),
('椭圆机', 6, 6, 0),
('卧推架', 4, 4, 1),
('深蹲架', 3, 3, 1),
('哑铃套装', 5, 5, 0),
('划船机', 4, 4, 0),
('综合训练器', 2, 2, 1);

-- ============================================================================
-- 视图和索引优化
-- ============================================================================

-- 创建用户会员状态视图
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

-- 创建设备可用性视图
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

-- ============================================================================
-- 触发器
-- ============================================================================

-- 器材预约成功后自动减少可用数量
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

-- 器材预约取消后自动恢复可用数量
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

-- ============================================================================
-- 完成提示
-- ============================================================================
SELECT 'Database schema initialized successfully!' AS message;
SELECT COUNT(*) AS total_tables FROM information_schema.tables WHERE table_schema = 'unmanned_gym';

