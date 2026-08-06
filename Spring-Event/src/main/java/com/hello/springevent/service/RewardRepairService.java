package com.hello.springevent.service;

import com.hello.springevent.domain.RewardRecord;
import com.hello.springevent.dto.RepairResponse;
import com.hello.springevent.mapper.RewardRecordMapper;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RewardRepairService {

    /** 奖励记录 Mapper，用于扫描待补偿记录。 */
    private final RewardRecordMapper rewardRecordMapper;

    /** 奖励结算服务，补偿时复用正常发奖逻辑。 */
    private final RewardSettlementService rewardSettlementService;

    /**
     * 多线程扫描并补偿 PENDING 奖励记录。
     *
     * @param limit 最大扫描数量
     * @param threadCount 补偿线程数
     * @return 补偿统计结果
     */
    public RepairResponse repair(Integer limit, Integer threadCount) {
        int safeLimit = limit == null ? 100 : Math.max(1, Math.min(limit, 500));
        int safeThreadCount = threadCount == null ? 4 : Math.max(1, Math.min(threadCount, 16));
        // 统一补发入口只扫描 PENDING 记录，SUCCESS 记录不会再次进入补偿链路。
        List<RewardRecord> pendingRecords = rewardRecordMapper.selectPending(safeLimit);

        ExecutorService executorService = Executors.newFixedThreadPool(safeThreadCount);
        try {
            List<Callable<Boolean>> tasks = pendingRecords.stream()
                    // 每条奖励复用 RewardSettlementService，确保正常发奖和补偿发奖走同一套幂等逻辑。
                    .map(record -> (Callable<Boolean>) () -> rewardSettlementService.settle(record.getId()))
                    .toList();
            int repaired = 0;
            for (Future<Boolean> future : executorService.invokeAll(tasks)) {
                if (Boolean.TRUE.equals(future.get())) {
                    repaired++;
                }
            }
            return new RepairResponse(pendingRecords.size(), repaired);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("奖励补偿任务被中断", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("奖励补偿任务执行失败", exception);
        } finally {
            executorService.shutdown();
        }
    }
}
