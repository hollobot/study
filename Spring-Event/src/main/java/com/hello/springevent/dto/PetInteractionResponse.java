package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 倒霉狗交互响应。
 *
 * @param actionType 交互动作类型
 * @param message 处理说明
 */
@Schema(description = "倒霉狗交互响应")
public record PetInteractionResponse(
        @Schema(description = "交互动作类型", example = "BATH") String actionType,
        @Schema(description = "处理说明", example = "洗澡完成，奖励和日志已交给 Spring Event 监听器处理") String message) {
}
