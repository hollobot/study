package com.hello.springevent.controller;

import com.hello.springevent.common.ApiResponse;
import com.hello.springevent.dto.RepairRequest;
import com.hello.springevent.dto.RepairResponse;
import com.hello.springevent.service.RewardRepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reward")
@Tag(name = "奖励补偿", description = "扫描 PENDING 奖励并进行幂等补发")
@RequiredArgsConstructor
public class RewardController {

    /** 奖励补偿服务。 */
    private final RewardRepairService rewardRepairService;

    /**
     * 触发奖励补偿任务，扫描 PENDING 奖励并幂等补发。
     *
     * @param request 补偿参数，为空时使用默认扫描数量和线程数
     * @return 补偿统计结果
     */
    @PostMapping("/repair")
    @Operation(summary = "补发漏发奖励", description = "多线程扫描 PENDING 奖励记录，基于幂等状态进行补偿结算")
    public ApiResponse<RepairResponse> repair(@RequestBody(required = false) RepairRequest request) {
        RepairRequest safeRequest = request == null ? new RepairRequest(null, null) : request;
        return ApiResponse.success(rewardRepairService.repair(safeRequest.limit(), safeRequest.threadCount()));
    }
}
