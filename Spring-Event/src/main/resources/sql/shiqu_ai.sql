CREATE DATABASE IF NOT EXISTS shiqu_ai_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shiqu_ai_demo;

CREATE TABLE IF NOT EXISTS sq_user_asset (
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    coin INT NOT NULL DEFAULT 0 COMMENT '金币余额',
    point INT NOT NULL DEFAULT 0 COMMENT '积分',
    experience INT NOT NULL DEFAULT 0 COMMENT '经验值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资产表';

CREATE TABLE IF NOT EXISTS sq_pet_profile (
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    level INT NOT NULL DEFAULT 1 COMMENT '宠物等级',
    satiety INT NOT NULL DEFAULT 0 COMMENT '饱食度，0-100',
    clean_value INT NOT NULL DEFAULT 60 COMMENT '清洁度，0-100',
    energy_value INT NOT NULL DEFAULT 60 COMMENT '精力值，0-100',
    growth_value INT NOT NULL DEFAULT 0 COMMENT '成长值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='倒霉狗养成档案';

CREATE TABLE IF NOT EXISTS sq_pet_interaction_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    action_type VARCHAR(32) NOT NULL COMMENT '交互动作类型',
    item_code VARCHAR(64) NULL COMMENT '交互道具编码',
    satiety_delta INT NOT NULL DEFAULT 0 COMMENT '饱食度变化值',
    clean_delta INT NOT NULL DEFAULT 0 COMMENT '清洁度变化值',
    energy_delta INT NOT NULL DEFAULT 0 COMMENT '精力值变化值',
    growth_delta INT NOT NULL DEFAULT 0 COMMENT '成长值变化值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_pet_interaction_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='倒霉狗交互日志';

CREATE TABLE IF NOT EXISTS sq_virtual_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_name VARCHAR(64) NOT NULL COMMENT '商品名',
    coin_price INT NOT NULL COMMENT '金币价格',
    reward_point INT NOT NULL DEFAULT 0 COMMENT '购买后奖励积分',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟商品表';

CREATE TABLE IF NOT EXISTS sq_purchase_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    product_id BIGINT NOT NULL COMMENT '商品 ID',
    coin_amount INT NOT NULL COMMENT '支付金币',
    status VARCHAR(20) NOT NULL COMMENT '订单状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_purchase_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='虚拟商品购买订单';

CREATE TABLE IF NOT EXISTS sq_reward_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    biz_type VARCHAR(32) NOT NULL COMMENT '奖励业务类型',
    biz_id VARCHAR(128) NOT NULL COMMENT '业务幂等 ID',
    coin_delta INT NOT NULL DEFAULT 0 COMMENT '金币奖励',
    point_delta INT NOT NULL DEFAULT 0 COMMENT '积分奖励',
    experience_delta INT NOT NULL DEFAULT 0 COMMENT '经验奖励',
    growth_delta INT NOT NULL DEFAULT 0 COMMENT '成长值奖励',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS',
    finish_time DATETIME NULL COMMENT '发放完成时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reward_biz (biz_type, biz_id),
    KEY idx_reward_status_id (status, id),
    KEY idx_reward_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励发放幂等表';

INSERT INTO sq_virtual_product (id, product_name, coin_price, reward_point)
VALUES
    (1, '倒霉狗帽子', 20, 2),
    (2, '倒霉狗背包', 35, 4),
    (3, '成长加速卡', 50, 8)
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    coin_price = VALUES(coin_price),
    reward_point = VALUES(reward_point);
