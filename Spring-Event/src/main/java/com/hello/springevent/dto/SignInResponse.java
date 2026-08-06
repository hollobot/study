package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * 签到响应。
 *
 * @param rewardRecordId 签到奖励记录 ID
 * @param signDate 签到日期
 * @param dayOfYear 一年中的第几天，用作 Bitmap offset + 1
 */
@Schema(description = "签到响应")
public record SignInResponse(
        @Schema(description = "签到奖励记录 ID", example = "3") Long rewardRecordId,
        @Schema(description = "签到日期", example = "2026-07-09") LocalDate signDate,
        @Schema(description = "一年中的第几天，用作 Bitmap offset + 1", example = "190") int dayOfYear) {
}
