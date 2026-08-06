package com.hello.springevent.event;

import com.hello.springevent.domain.PetInteractionLog;
import com.hello.springevent.mapper.PetInteractionLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 倒霉狗交互日志监听器。
 * <p>
 * 监听事件：PetInteractionEvent。
 * 执行时机：交互主事务提交后执行，避免主事务回滚后仍写入日志。
 * 事务策略：REQUIRES_NEW，日志写入独立提交，不污染交互主事务。
 * 后续扩展：如果要做运营埋点、行为统计，可以参考该监听器新增独立 listener。
 */
@Component
@RequiredArgsConstructor
public class PetInteractionLogListener {

    /** 交互日志 Mapper，用于记录用户每次和倒霉狗的互动。 */
    private final PetInteractionLogMapper petInteractionLogMapper;

    /**
     * 监听倒霉狗交互事件，并在独立事务中写入交互日志。
     *
     * @param event 倒霉狗交互事件
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPetInteractionEvent(PetInteractionEvent event) {
        // 日志监听器只关心事件事实本身；通过 actionType 落库后可以按动作做运营统计。
        PetInteractionLog interactionLog = new PetInteractionLog();
        interactionLog.setUserId(event.userId());
        interactionLog.setActionType(event.action().name());
        interactionLog.setItemCode(event.itemCode());
        interactionLog.setSatietyDelta(event.action().getSatietyDelta());
        interactionLog.setCleanDelta(event.action().getCleanDelta());
        interactionLog.setEnergyDelta(event.action().getEnergyDelta());
        interactionLog.setGrowthDelta(event.action().getPetGrowthDelta());
        petInteractionLogMapper.insertLog(interactionLog);
    }
}
