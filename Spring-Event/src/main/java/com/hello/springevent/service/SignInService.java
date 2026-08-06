package com.hello.springevent.service;

import com.hello.springevent.common.BizException;
import com.hello.springevent.domain.RewardBizType;
import com.hello.springevent.domain.RewardRecord;
import com.hello.springevent.dto.SignInCalendarResponse;
import com.hello.springevent.dto.SignInRequest;
import com.hello.springevent.dto.SignInResponse;
import com.hello.springevent.event.RewardSettlementEvent;
import com.hello.springevent.lock.UserLockTemplate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class SignInService {

    /** Redisson 客户端，用于操作 Redis Bitmap。 */
    private final RedissonClient redissonClient;

    /** 用户维度锁模板，避免同一用户重复签到并发写入。 */
    private final UserLockTemplate userLockTemplate;

    /** 奖励记录命令服务，负责创建签到奖励记录。 */
    private final RewardRecordCommandService rewardRecordCommandService;

    /** Spring 事件发布器，用于发布签到奖励结算事件。 */
    private final ApplicationEventPublisher eventPublisher;

    /** 编程式事务模板，保证签到奖励记录和事件提交一致。 */
    private final TransactionTemplate transactionTemplate;

    /**
     * 执行用户每日签到。
     *
     * @param request 签到请求
     * @return 签到结果
     */
    public SignInResponse signIn(SignInRequest request) {
        Long userId = requireUserId(request.userId());
        return userLockTemplate.execute(
                "sign-in",
                userId,
                () -> transactionTemplate.execute(status -> doSignIn(userId, LocalDate.now())));
    }

    /**
     * 在签到主事务内校验 Bitmap、创建奖励记录并发布奖励结算事件。
     *
     * @param userId 用户 ID
     * @param signDate 签到日期
     * @return 签到结果
     */
    private SignInResponse doSignIn(Long userId, LocalDate signDate) {
        RBitSet bitSet = redissonClient.getBitSet(signInKey(userId, signDate.getYear()));
        int offset = signDate.getDayOfYear() - 1;
        if (bitSet.get(offset)) {
            throw new BizException("今日已签到");
        }

        RewardRecord rewardRecord = rewardRecordCommandService.createPending(
                userId,
                RewardBizType.SIGN_IN,
                userId + ":" + signDate,
                5,
                2,
                5,
                3);
        // 年度签到状态只占 365/366 个 bit，查询和写入都退化为位运算。
        bitSet.set(offset, true);
        eventPublisher.publishEvent(new RewardSettlementEvent(rewardRecord.getId()));
        return new SignInResponse(rewardRecord.getId(), signDate, signDate.getDayOfYear());
    }

    /**
     * 查询用户指定年份的签到日历。
     *
     * @param userId 用户 ID
     * @param year 年份
     * @return 签到日历
     */
    public SignInCalendarResponse getCalendar(Long userId, int year) {
        requireUserId(userId);
        RBitSet bitSet = redissonClient.getBitSet(signInKey(userId, year));
        List<String> signedDates = new ArrayList<>();
        int yearLength = LocalDate.of(year, 1, 1).lengthOfYear();
        for (int offset = 0; offset < yearLength; offset++) {
            if (bitSet.get(offset)) {
                signedDates.add(LocalDate.ofYearDay(year, offset + 1).toString());
            }
        }
        return new SignInCalendarResponse(userId, year, signedDates.size(), signedDates);
    }

    /**
     * 生成用户年度签到 Bitmap 的 Redis key。
     *
     * @param userId 用户 ID
     * @param year 年份
     * @return Redis Bitmap key
     */
    private String signInKey(Long userId, int year) {
        return "shiqu:sign-in:" + year + ":" + userId;
    }

    /**
     * 校验用户 ID。
     *
     * @param userId 用户 ID
     * @return 校验后的用户 ID
     */
    private Long requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException("userId 不能为空");
        }
        return userId;
    }
}
