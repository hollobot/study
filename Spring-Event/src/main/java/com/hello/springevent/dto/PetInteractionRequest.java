package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 倒霉狗交互请求。
 *
 * @param userId 用户 ID
 * @param actionType 交互动作类型，支持 FEED/BATH/SLEEP/DRINK/EXERCISE/PLAY
 * @param itemCode 交互道具编码，可为空
 */
@Schema(description = "倒霉狗交互请求")
public record PetInteractionRequest(
        @Schema(description = "用户 ID", example = "10001") Long userId,
        @Schema(description = "交互动作类型", example = "BATH") String actionType,
        @Schema(description = "交互道具编码，可为空", example = "SOAP") String itemCode) {
}
