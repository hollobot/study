package com.hello.springevent.controller;

import com.hello.springevent.common.ApiResponse;
import com.hello.springevent.dto.PurchaseRequest;
import com.hello.springevent.dto.PurchaseResponse;
import com.hello.springevent.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@Tag(name = "虚拟商城", description = "虚拟商品购买，用户维度 Redisson 锁保护金币扣减")
@RequiredArgsConstructor
public class ShopController {

    /** 虚拟商品购买服务。 */
    private final PurchaseService purchaseService;

    /**
     * 购买虚拟商品，扣减金币并生成订单。
     *
     * @param request 购买请求
     * @return 购买结果
     */
    @PostMapping("/purchase")
    @Operation(summary = "购买虚拟商品", description = "扣减金币、生成订单，并通过事件发放购买积分奖励")
    public ApiResponse<PurchaseResponse> purchase(@RequestBody PurchaseRequest request) {
        return ApiResponse.success(purchaseService.purchase(request));
    }
}
