package com.hello.springevent.service;

import com.hello.springevent.domain.RewardRecord;
import com.hello.springevent.mapper.RewardRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RewardRecordCommandService {

    /** 奖励记录 Mapper。 */
    private final RewardRecordMapper rewardRecordMapper;

    /**
     * 创建 PENDING 奖励记录；如果幂等键已存在，则复用已有记录。
     *
     * @param userId 用户 ID
     * @param bizType 奖励业务类型
     * @param bizId 业务幂等 ID
     * @param coinDelta 金币变化值
     * @param pointDelta 积分变化值
     * @param experienceDelta 经验变化值
     * @param growthDelta 成长值变化值
     * @return 创建或复用的奖励记录
     */
    public RewardRecord createPending(
            Long userId,
            String bizType,
            String bizId,
            int coinDelta,
            int pointDelta,
            int experienceDelta,
            int growthDelta) {
        RewardRecord rewardRecord = new RewardRecord();
        rewardRecord.setUserId(userId);
        rewardRecord.setBizType(bizType);
        rewardRecord.setBizId(bizId);
        rewardRecord.setCoinDelta(coinDelta);
        rewardRecord.setPointDelta(pointDelta);
        rewardRecord.setExperienceDelta(experienceDelta);
        rewardRecord.setGrowthDelta(growthDelta);

        try {
            // uk_reward_biz 保证同一业务动作只生成一条奖励记录，是后续统一补发的幂等基础。
            rewardRecordMapper.insertPending(rewardRecord);
        } catch (DuplicateKeyException exception) {
            // 幂等键冲突说明业务请求已创建奖励记录，后续直接复用原记录。
        }
        RewardRecord existedRecord = rewardRecordMapper.selectByBiz(bizType, bizId);
        if (existedRecord == null) {
            throw new IllegalStateException("奖励记录创建失败");
        }
        return existedRecord;
    }
}
