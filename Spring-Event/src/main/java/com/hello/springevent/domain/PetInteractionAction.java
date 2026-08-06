package com.hello.springevent.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PetInteractionAction {

    /** 喂食，主要提升饱食度。 */
    FEED("喂食", 18, 0, 0, 5, 1, 1, 3, 0),

    /** 洗澡，主要提升清洁度。 */
    BATH("洗澡", 0, 25, 0, 3, 1, 1, 2, 0),

    /** 睡觉，主要恢复精力。 */
    SLEEP("睡觉", -5, 0, 30, 4, 2, 1, 4, 1),

    /** 喝水，小幅提升饱食度和精力。 */
    DRINK("喝水", 5, 0, 8, 2, 1, 1, 2, 0),

    /** 运动，消耗饱食度和精力，但提升成长值。 */
    EXERCISE("运动", -8, -3, -10, 12, 2, 2, 5, 2),

    /** 玩耍，均衡提升成长值和亲密互动收益。 */
    PLAY("玩耍", -3, -2, -5, 8, 1, 2, 4, 1);

    /** 动作展示名称。 */
    private final String displayName;

    /** 饱食度变化值。 */
    private final int satietyDelta;

    /** 清洁度变化值。 */
    private final int cleanDelta;

    /** 精力值变化值。 */
    private final int energyDelta;

    /** 宠物成长值变化值。 */
    private final int petGrowthDelta;

    /** 动作完成后的金币奖励。 */
    private final int coinReward;

    /** 动作完成后的积分奖励。 */
    private final int pointReward;

    /** 动作完成后的经验奖励。 */
    private final int experienceReward;

    /** 动作完成后的额外成长值奖励。 */
    private final int rewardGrowthDelta;
}
