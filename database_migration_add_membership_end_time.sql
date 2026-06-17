-- 为用户表添加会员结束时间字段
ALTER TABLE `user` ADD COLUMN `membership_end_time` DATE NULL COMMENT '会员结束时间';

-- 为现有用户初始化membership_end_time（如果有有效订单）
UPDATE `user` u
INNER JOIN (
    SELECT uid, MAX(order_end_time) as max_end_time
    FROM `order`
    WHERE is_deleted = 'n'
    AND order_end_time >= CURDATE()
    GROUP BY uid
) o ON u.id = o.uid
SET u.membership_end_time = o.max_end_time;
