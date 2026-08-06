package com.hello.springevent.event;

import com.hello.springevent.domain.PetInteractionAction;

/**
 * 倒霉狗交互事件。
 * 事件中直接携带 PetInteractionAction，监听器无需解析请求参数即可判断具体动作。
 * <p>
 * 发布位置：PetInteractionService 在完成宠物状态更新后发布。
 * 适用场景：凡是“用户已经完成一次倒霉狗交互”之后要扩展的后置能力，都优先监听该事件。
 * 扩展示例：奖励发放、交互日志、每日任务、成就、亲密度、运营埋点。
 *
 * @param userId 用户 ID
 * @param action 交互动作配置，监听器通过该字段区分 FEED/BATH/SLEEP/DRINK/EXERCISE/PLAY
 * @param itemCode 交互道具编码，可为空
 * @param bizId 本次交互业务幂等 ID
 */
public record PetInteractionEvent(Long userId, PetInteractionAction action, String itemCode, String bizId) {
}
