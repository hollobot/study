package com.hello.springevent.service;

import com.hello.springevent.common.BizException;
import com.hello.springevent.domain.PetInteractionAction;
import com.hello.springevent.dto.PetInteractionRequest;
import com.hello.springevent.dto.PetInteractionResponse;
import com.hello.springevent.event.PetInteractionEvent;
import com.hello.springevent.lock.UserLockTemplate;
import com.hello.springevent.mapper.PetMapper;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class PetInteractionService {

    /** 用户维度锁模板，避免同一用户多个交互动作并发写入宠物状态。 */
    private final UserLockTemplate userLockTemplate;

    /** 倒霉狗档案 Mapper。 */
    private final PetMapper petMapper;

    /** Spring 事件发布器，用于把奖励、日志等后置逻辑交给监听器。 */
    private final ApplicationEventPublisher eventPublisher;

    /** 编程式事务模板，确保状态变更提交后再触发事件监听器。 */
    private final TransactionTemplate transactionTemplate;

    /**
     * 执行倒霉狗交互入口，负责用户锁和主事务编排。
     *
     * @param request 交互请求
     * @return 交互处理结果
     */
    public PetInteractionResponse interact(PetInteractionRequest request) {
        Long userId = requireUserId(request.userId());
        PetInteractionAction action = parseAction(request.actionType());
        return userLockTemplate.execute(
                "pet-interaction",
                userId,
                // 交互状态变更和事件发布放在同一个事务里；监听器使用 AFTER_COMMIT，只有主事务提交成功才会处理奖励和日志。
                () -> transactionTemplate.execute(status -> doInteract(userId, action, request.itemCode())));
    }

    /**
     * 在主事务内更新宠物状态并发布交互事件。
     *
     * @param userId 用户 ID
     * @param action 交互动作配置
     * @param itemCode 道具编码，可为空
     * @return 交互处理结果
     */
    private PetInteractionResponse doInteract(Long userId, PetInteractionAction action, String itemCode) {
        petMapper.ensurePet(userId);
        petMapper.applyInteraction(
                userId,
                action.getSatietyDelta(),
                action.getCleanDelta(),
                action.getEnergyDelta(),
                action.getPetGrowthDelta());

        // 主流程只发布“发生了什么动作”这一事实；监听器通过 event.action() 判断 FEED/BATH/SLEEP 等具体动作。
        String bizId = userId + ":" + action.name() + ":" + UUID.randomUUID();
        eventPublisher.publishEvent(new PetInteractionEvent(userId, action, itemCode, bizId));
        return new PetInteractionResponse(
                action.name(),
                action.getDisplayName() + "完成，奖励和日志已交给 Spring Event 监听器处理");
    }

    /**
     * 将接口传入的动作字符串转换为枚举配置。
     *
     * @param actionType 动作字符串
     * @return 交互动作枚举
     */
    private PetInteractionAction parseAction(String actionType) {
        if (actionType == null || actionType.isBlank()) {
            throw new BizException("actionType 不能为空");
        }
        try {
            return PetInteractionAction.valueOf(actionType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BizException("不支持的交互动作：" + actionType);
        }
    }

    /**
     * 校验用户 ID。
     *
     * @param userId 用户 ID
     * @return 校验后的用户 ID
     */
    private Long requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException("userId 不能为空");
        }
        return userId;
    }
}
