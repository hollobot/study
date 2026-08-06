package com.hello.springevent.mapper;

import com.hello.springevent.domain.UserOverview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetMapper {

    /**
     * 初始化用户的倒霉狗档案；如果已存在则不修改业务数据。
     *
     * @param userId 用户 ID
     */
    void ensurePet(@Param("userId") Long userId);

    /**
     * 应用一次倒霉狗交互动作带来的状态变化。
     *
     * @param userId 用户 ID
     * @param satietyDelta 饱食度变化值
     * @param cleanDelta 清洁度变化值
     * @param energyDelta 精力值变化值
     * @param growthDelta 成长值变化值
     */
    void applyInteraction(
            @Param("userId") Long userId,
            @Param("satietyDelta") int satietyDelta,
            @Param("cleanDelta") int cleanDelta,
            @Param("energyDelta") int energyDelta,
            @Param("growthDelta") int growthDelta);

    /**
     * 增加倒霉狗成长值奖励。
     *
     * @param userId 用户 ID
     * @param growthDelta 成长值变化值
     */
    void addGrowth(@Param("userId") Long userId, @Param("growthDelta") int growthDelta);

    /**
     * 查询用户资产和倒霉狗状态总览。
     *
     * @param userId 用户 ID
     * @return 用户资产和宠物状态
     */
    UserOverview selectOverview(@Param("userId") Long userId);
}
