package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 奖励补偿响应。
 *
 * @param scanned 扫描到的 PENDING 记录数
 * @param repaired 实际补发成功数
 */
@Schema(description = "奖励补偿响应")
public record RepairResponse(
        @Schema(description = "扫描到的 PENDING 记录数", example = "10") int scanned,
        @Schema(description = "实际补发成功数", example = "10") int repaired) {
}
