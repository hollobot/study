package com.hello.springevent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAssetMapper {

    /**
     * 初始化用户资产；如果已存在则不修改余额。
     *
     * @param userId 用户 ID
     */
    void ensureUserAsset(@Param("userId") Long userId);

    /**
     * 扣减用户金币，余额不足时不会扣减。
     *
     * @param userId 用户 ID
     * @param coin 扣减金币数量
     * @return 影响行数，1 表示扣减成功，0 表示余额不足或用户不存在
     */
    int deductCoin(@Param("userId") Long userId, @Param("coin") int coin);

    /**
     * 发放金币、积分和经验奖励。
     *
     * @param userId 用户 ID
     * @param coinDelta 金币变化值
     * @param pointDelta 积分变化值
     * @param experienceDelta 经验变化值
     */
    void addReward(
            @Param("userId") Long userId,
            @Param("coinDelta") int coinDelta,
            @Param("pointDelta") int pointDelta,
            @Param("experienceDelta") int experienceDelta);

    /**
     * 查询用户金币余额。
     *
     * @param userId 用户 ID
     * @return 金币余额，不存在时返回 null
     */
    Integer selectCoin(@Param("userId") Long userId);
}
