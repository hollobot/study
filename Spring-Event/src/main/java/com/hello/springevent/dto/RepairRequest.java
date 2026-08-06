package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 奖励补偿请求。
 *
 * @param limit 最多扫描多少条 PENDING 奖励
 * @param threadCount 补偿线程数
 */
@Schema(description = "奖励补偿请求")
public record RepairRequest(
        @Schema(description = "最多扫描多少条 PENDING 奖励，默认 100，最大 500", example = "100") Integer limit,
        @Schema(description = "补偿线程数，默认 4，最大 16", example = "4") Integer threadCount) {
}
