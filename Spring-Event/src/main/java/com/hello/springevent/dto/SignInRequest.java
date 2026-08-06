package com.hello.springevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 签到请求。
 *
 * @param userId 用户 ID
 */
@Schema(description = "签到请求")
public record SignInRequest(
        @Schema(description = "用户 ID", example = "10001") Long userId) {
}
