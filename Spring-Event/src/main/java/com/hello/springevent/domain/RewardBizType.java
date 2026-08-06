package com.hello.springevent.domain;

public final class RewardBizType {

    /** 倒霉狗交互奖励业务类型。 */
    public static final String PET_INTERACTION = "PET_INTERACTION";

    /** 签到奖励业务类型。 */
    public static final String SIGN_IN = "SIGN_IN";

    /** 虚拟商品购买奖励业务类型。 */
    public static final String PURCHASE = "PURCHASE";

    /**
     * 工具常量类不允许实例化。
     */
    private RewardBizType() {
    }
}
