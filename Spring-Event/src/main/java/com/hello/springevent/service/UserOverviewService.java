package com.hello.springevent.service;

import com.hello.springevent.common.BizException;
import com.hello.springevent.domain.UserOverview;
import com.hello.springevent.mapper.PetMapper;
import com.hello.springevent.mapper.UserAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserOverviewService {

    /** 用户资产 Mapper。 */
    private final UserAssetMapper userAssetMapper;

    /** 倒霉狗档案 Mapper。 */
    private final PetMapper petMapper;

    /**
     * 查询用户资产和倒霉狗状态，不存在时先初始化默认档案。
     *
     * @param userId 用户 ID
     * @return 用户资产和宠物状态
     */
    public UserOverview getOverview(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException("userId 不能为空");
        }
        userAssetMapper.ensureUserAsset(userId);
        petMapper.ensurePet(userId);
        return petMapper.selectOverview(userId);
    }
}
