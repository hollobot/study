package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 购买虚拟商品请求。
 *
 * @param userId 用户 ID
 * @param productId 商品 ID，SQL 默认初始化 1/2/3
 */
@Schema(description = "购买虚拟商品请求")
public record PurchaseRequest(
        @Schema(description = "用户 ID", example = "10001") Long userId,
        @Schema(description = "商品 ID，SQL 默认初始化 1/2/3", example = "1") Long productId) {
}
