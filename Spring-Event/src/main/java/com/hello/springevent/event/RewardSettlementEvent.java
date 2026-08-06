package com.hello.springevent.event;

/**
 * 奖励结算事件。
 * <p>
 * 发布位置：业务方已经创建 sq_reward_record 后发布。
 * 适用场景：只知道奖励记录 ID，后续统一走 RewardSettlementService 发奖。
 * 注意：倒霉狗交互目前直接监听 PetInteractionEvent 创建并结算奖励；该事件保留给签到、购买等已创建奖励记录的场景复用。
 *
 * @param rewardRecordId 待结算的奖励记录 ID
 */
public record RewardSettlementEvent(Long rewardRecordId) {
}
