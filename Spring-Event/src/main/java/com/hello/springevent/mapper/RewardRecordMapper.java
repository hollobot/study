package com.hello.springevent.mapper;

import com.hello.springevent.domain.RewardRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RewardRecordMapper {

    /**
     * 插入 PENDING 状态的奖励记录。
     *
     * @param rewardRecord 奖励记录
     * @return 影响行数
     */
    int insertPending(RewardRecord rewardRecord);

    /**
     * 根据业务类型和业务幂等 ID 查询奖励记录。
     *
     * @param bizType 奖励业务类型
     * @param bizId 业务幂等 ID
     * @return 奖励记录，不存在时返回 null
     */
    RewardRecord selectByBiz(@Param("bizType") String bizType, @Param("bizId") String bizId);

    /**
     * 根据奖励记录 ID 查询并加行锁，用于防止并发重复发奖。
     *
     * @param rewardRecordId 奖励记录 ID
     * @return 奖励记录，不存在时返回 null
     */
    RewardRecord selectByIdForUpdate(@Param("rewardRecordId") Long rewardRecordId);

    /**
     * 查询待补偿的 PENDING 奖励记录。
     *
     * @param limit 最大查询数量
     * @return 待补偿奖励记录列表
     */
    List<RewardRecord> selectPending(@Param("limit") int limit);

    /**
     * 将奖励记录标记为已发放。
     *
     * @param rewardRecordId 奖励记录 ID
     */
    void markSuccess(@Param("rewardRecordId") Long rewardRecordId);
}
