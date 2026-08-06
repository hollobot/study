package com.hello.springevent.controller;

import com.hello.springevent.common.ApiResponse;
import com.hello.springevent.domain.UserOverview;
import com.hello.springevent.service.UserOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户资产", description = "查询金币、积分、经验和倒霉狗成长状态")
@RequiredArgsConstructor
public class UserAssetController {

    /** 用户资产和宠物状态查询服务。 */
    private final UserOverviewService userOverviewService;

    /**
     * 查询用户资产和倒霉狗状态。
     *
     * @param userId 用户 ID
     * @return 用户资产和宠物状态
     */
    @GetMapping("/{userId}/assets")
    @Operation(summary = "查询用户资产和宠物状态")
    public ApiResponse<UserOverview> assets(@PathVariable Long userId) {
        return ApiResponse.success(userOverviewService.getOverview(userId));
    }
}
