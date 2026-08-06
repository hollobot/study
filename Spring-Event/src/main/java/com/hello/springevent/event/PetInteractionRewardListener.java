package com.hello.springevent.event;

import com.hello.springevent.domain.RewardBizType;
import com.hello.springevent.domain.RewardRecord;
import com.hello.springevent.service.RewardRecordCommandService;
import com.hello.springevent.service.RewardSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 倒霉狗交互奖励监听器。
 * <p>
 * 监听事件：PetInteractionEvent。
 * 执行时机：交互主事务提交后执行，保证宠物状态变更成功后才发奖励。
 * 事务策略：REQUIRES_NEW，奖励创建和发放独立提交。
 * 动作判断：通过 event.action() 获取 FEED/BATH/SLEEP 等动作及其奖励配置。
 * 后续扩展：新增动作时优先扩展 PetInteractionAction；新增其他后置业务时新增监听器，不修改本类。
 */
@Component
@RequiredArgsConstructor
public class PetInteractionRewardListener {

    /** 奖励记录命令服务，负责为交互行为创建幂等奖励记录。 */
    private final RewardRecordCommandService rewardRecordCommandService;

    /** 奖励结算服务，负责把交互奖励写入用户资产。 */
    private final RewardSettlementService rewardSettlementService;

    /**
     * 监听倒霉狗交互事件，并在独立事务中创建和结算交互奖励。
     *
     * @param event 倒霉狗交互事件
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPetInteractionEvent(PetInteractionEvent event) {
        // event.action() 已经包含动作对应的奖励配置，新增动作时优先扩展 PetInteractionAction 枚举。
        RewardRecord rewardRecord = rewardRecordCommandService.createPending(
                event.userId(),
                RewardBizType.PET_INTERACTION,
                event.bizId(),
                event.action().getCoinReward(),
                event.action().getPointReward(),
                event.action().getExperienceReward(),
                event.action().getRewardGrowthDelta());
        rewardSettlementService.settle(rewardRecord.getId());
    }
}
