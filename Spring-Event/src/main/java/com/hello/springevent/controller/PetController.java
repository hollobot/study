package com.hello.springevent.controller;

import com.hello.springevent.common.ApiResponse;
import com.hello.springevent.dto.FeedRequest;
import com.hello.springevent.dto.PetInteractionRequest;
import com.hello.springevent.dto.PetInteractionResponse;
import com.hello.springevent.service.PetInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pet")
@Tag(name = "倒霉狗养成", description = "喂食、洗澡、睡觉、喝水、运动等交互动作，通过 Spring Event 解耦奖励和日志")
@RequiredArgsConstructor
public class PetController {

    /** 倒霉狗交互业务服务。 */
    private final PetInteractionService petInteractionService;

    /**
     * 兼容旧喂食接口，将 foodCode 转成 FEED 交互动作。
     *
     * @param request 喂食请求
     * @return 倒霉狗交互响应
     */
    @PostMapping("/feed")
    @Operation(summary = "喂食倒霉狗", description = "兼容喂食入口，内部转成 FEED 交互动作")
    public ApiResponse<PetInteractionResponse> feed(@RequestBody FeedRequest request) {
        PetInteractionRequest interactionRequest = new PetInteractionRequest(
                request.userId(),
                "FEED",
                request.foodCode());
        return ApiResponse.success(petInteractionService.interact(interactionRequest));
    }

    /**
     * 执行倒霉狗通用交互动作。
     *
     * @param request 交互请求，包含用户 ID、动作类型和道具编码
     * @return 倒霉狗交互响应
     */
    @PostMapping("/interact")
    @Operation(
            summary = "和倒霉狗交互",
            description = "支持 FEED/BATH/SLEEP/DRINK/EXERCISE/PLAY，主流程只更新状态，奖励和日志由事件监听器处理")
    public ApiResponse<PetInteractionResponse> interact(@RequestBody PetInteractionRequest request) {
        return ApiResponse.success(petInteractionService.interact(request));
    }
}
