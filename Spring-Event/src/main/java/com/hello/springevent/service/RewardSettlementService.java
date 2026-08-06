package com.hello.springevent.service;

import com.hello.springevent.domain.RewardRecord;
import com.hello.springevent.mapper.PetMapper;
import com.hello.springevent.mapper.RewardRecordMapper;
import com.hello.springevent.mapper.UserAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardSettlementService {

    /** 奖励记录 Mapper，用于查询和更新奖励状态。 */
    private final RewardRecordMapper rewardRecordMapper;

    /** 用户资产 Mapper，用于写入金币、积分和经验奖励。 */
    private final UserAssetMapper userAssetMapper;

    /** 倒霉狗档案 Mapper，用于写入成长值奖励。 */
    private final PetMapper petMapper;

    /**
     * 结算奖励记录，发放金币、积分、经验和成长值。
     *
     * @param rewardRecordId 奖励记录 ID
     * @return true 表示本次完成发放，false 表示记录不存在或已发放
     */
    @Transactional
    public boolean settle(Long rewardRecordId) {
        // FOR UPDATE 锁住单条奖励记录，防止补偿任务和事件监听器并发重复发放。
        RewardRecord record = rewardRecordMapper.selectByIdForUpdate(rewardRecordId);
        if (record == null || "SUCCESS".equals(record.getStatus())) {
            return false;
        }

        userAssetMapper.addReward(
                record.getUserId(),
                record.getCoinDelta(),
                record.getPointDelta(),
                record.getExperienceDelta());
        if (record.getGrowthDelta() > 0) {
            petMapper.addGrowth(record.getUserId(), record.getGrowthDelta());
        }
        rewardRecordMapper.markSuccess(record.getId());
        return true;
    }
}
