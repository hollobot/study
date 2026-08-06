package com.hello.springevent.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RewardRecord {

    /** 奖励记录 ID。 */
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 业务类型，例如 FEED、SIGN_IN、PURCHASE。 */
    private String bizType;

    /** 业务幂等 ID，同一业务只允许创建一条奖励记录。 */
    private String bizId;

    /** 金币变更数量，正数表示奖励，负数表示扣减。 */
    private int coinDelta;

    /** 积分变更数量。 */
    private int pointDelta;

    /** 经验变更数量。 */
    private int experienceDelta;

    /** 倒霉狗成长值变更数量。 */
    private int growthDelta;

    /** 奖励状态，PENDING 表示待发放，SUCCESS 表示已发放。 */
    private String status;

    /** 奖励记录创建时间。 */
    private LocalDateTime createTime;

}
