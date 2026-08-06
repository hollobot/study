package com.hello.springevent.domain;

import lombok.Data;

@Data
public class UserOverview {

    /** 用户 ID。 */
    private Long userId;

    /** 金币余额。 */
    private int coin;

    /** 积分余额。 */
    private int point;

    /** 经验值。 */
    private int experience;

    /** 倒霉狗等级。 */
    private int petLevel;

    /** 倒霉狗饱食度。 */
    private int petSatiety;

    /** 倒霉狗清洁度。 */
    private int petCleanValue;

    /** 倒霉狗精力值。 */
    private int petEnergyValue;

    /** 倒霉狗成长值。 */
    private int petGrowthValue;

}
