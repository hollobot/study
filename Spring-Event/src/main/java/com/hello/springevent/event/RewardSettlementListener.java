package com.hello.springevent.event;

import com.hello.springevent.service.RewardSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 通用奖励结算监听器。
 * <p>
 * 监听事件：RewardSettlementEvent。
 * 执行时机：发布方事务提交后执行，保证奖励记录已经落库。
 * 职责边界：只根据 rewardRecordId 调用 RewardSettlementService，不关心奖励来自签到、购买还是其他业务。
 * 后续扩展：如果某业务已经提前创建 reward_record，只需要发布 RewardSettlementEvent 即可复用该监听器。
 */
@Component
@RequiredArgsConstructor
public class RewardSettlementListener {

    /** 奖励结算服务，负责把 PENDING 奖励实际写入用户资产。 */
    private final RewardSettlementService rewardSettlementService;

    /**
     * 监听通用奖励结算事件，并调用奖励结算服务发奖。
     *
     * @param event 奖励结算事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRewardSettlementEvent(RewardSettlementEvent event) {
        rewardSettlementService.settle(event.rewardRecordId());
    }
}
