package com.hello.springevent.controller;

import com.hello.springevent.common.ApiResponse;
import com.hello.springevent.dto.SignInCalendarResponse;
import com.hello.springevent.dto.SignInRequest;
import com.hello.springevent.dto.SignInResponse;
import com.hello.springevent.service.SignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sign-in")
@Tag(name = "年度签到", description = "基于 Redis Bitmap 的每日签到与日历查询")
@RequiredArgsConstructor
public class SignInController {

    /** 签到业务服务。 */
    private final SignInService signInService;

    /**
     * 执行每日签到。
     *
     * @param request 签到请求
     * @return 签到结果
     */
    @PostMapping
    @Operation(summary = "每日签到", description = "使用年度位图记录签到状态，并发布签到奖励结算事件")
    public ApiResponse<SignInResponse> signIn(@RequestBody SignInRequest request) {
        return ApiResponse.success(signInService.signIn(request));
    }

    /**
     * 查询指定用户某一年的签到日历。
     *
     * @param userId 用户 ID
     * @param year 年份，为空时查询当前年份
     * @return 签到日历
     */
    @GetMapping("/calendar/{userId}")
    @Operation(summary = "查询签到日历", description = "读取 Redis Bitmap，返回指定年份所有已签到日期")
    public ApiResponse<SignInCalendarResponse> calendar(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer year) {
        int queryYear = year == null ? Year.now().getValue() : year;
        return ApiResponse.success(signInService.getCalendar(userId, queryYear));
    }
}
