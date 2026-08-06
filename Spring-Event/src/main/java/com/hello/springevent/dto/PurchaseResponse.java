package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 购买虚拟商品响应。
 *
 * @param orderId 订单 ID
 * @param rewardRecordId 购买奖励记录 ID
 * @param paidCoin 本次支付金币
 */
@Schema(description = "购买虚拟商品响应")
public record PurchaseResponse(
        @Schema(description = "订单 ID", example = "1") Long orderId,
        @Schema(description = "购买奖励记录 ID", example = "2") Long rewardRecordId,
        @Schema(description = "本次支付金币", example = "20") int paidCoin) {
}
