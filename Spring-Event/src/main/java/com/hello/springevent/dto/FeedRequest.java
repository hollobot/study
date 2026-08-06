package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 喂食请求。
 *
 * @param userId 用户 ID
 * @param foodCode 食物编码，MEAT 奖励更高，其他值按普通食物处理
 */
@Schema(description = "喂食请求")
public record FeedRequest(
        @Schema(description = "用户 ID", example = "10001") Long userId,
        @Schema(description = "食物编码，MEAT 奖励更高，其他值按普通食物处理", example = "MEAT") String foodCode) {
}
