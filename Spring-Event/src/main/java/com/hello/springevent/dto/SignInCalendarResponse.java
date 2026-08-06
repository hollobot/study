package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 签到日历响应。
 *
 * @param userId 用户 ID
 * @param year 年份
 * @param signedCount 年度已签到天数
 * @param signedDates 已签到日期列表
 */
@Schema(description = "签到日历响应")
public record SignInCalendarResponse(
        @Schema(description = "用户 ID", example = "10001") Long userId,
        @Schema(description = "年份", example = "2026") int year,
        @Schema(description = "年度已签到天数", example = "12") int signedCount,
        @Schema(description = "已签到日期列表") List<String> signedDates) {
}
