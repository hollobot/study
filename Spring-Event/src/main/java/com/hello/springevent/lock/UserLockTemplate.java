package com.hello.springevent.lock;

import com.hello.springevent.common.BizException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLockTemplate {

    /** Redisson 客户端，用于创建用户维度分布式锁。 */
    private final RedissonClient redissonClient;

    /**
     * 按用户维度加锁并执行业务逻辑。
     *
     * @param scene 业务场景，用于区分签到、购买、交互等锁
     * @param userId 用户 ID
     * @param supplier 锁内执行的业务逻辑
     * @param <T> 业务返回类型
     * @return 业务执行结果
     */
    public <T> T execute(String scene, Long userId, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock("shiqu:user:" + userId + ":" + scene);
        boolean locked = false;
        try {
            // 以用户维度加锁，避免同一用户的金币扣减和奖励结算并发写入。
            locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BizException("系统繁忙，请稍后重试");
            }
            return supplier.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException("获取用户锁被中断");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
