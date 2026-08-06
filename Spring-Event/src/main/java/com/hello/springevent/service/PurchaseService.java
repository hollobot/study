package com.hello.springevent.service;

import com.hello.springevent.common.BizException;
import com.hello.springevent.domain.Product;
import com.hello.springevent.domain.PurchaseOrder;
import com.hello.springevent.domain.RewardBizType;
import com.hello.springevent.domain.RewardRecord;
import com.hello.springevent.dto.PurchaseRequest;
import com.hello.springevent.dto.PurchaseResponse;
import com.hello.springevent.event.RewardSettlementEvent;
import com.hello.springevent.lock.UserLockTemplate;
import com.hello.springevent.mapper.ProductMapper;
import com.hello.springevent.mapper.PurchaseOrderMapper;
import com.hello.springevent.mapper.UserAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    /** 用户维度锁模板，保护金币扣减和订单创建。 */
    private final UserLockTemplate userLockTemplate;

    /** 虚拟商品 Mapper。 */
    private final ProductMapper productMapper;

    /** 用户资产 Mapper。 */
    private final UserAssetMapper userAssetMapper;

    /** 购买订单 Mapper。 */
    private final PurchaseOrderMapper purchaseOrderMapper;

    /** 奖励记录命令服务，负责创建购买奖励记录。 */
    private final RewardRecordCommandService rewardRecordCommandService;

    /** Spring 事件发布器，用于发布购买奖励结算事件。 */
    private final ApplicationEventPublisher eventPublisher;

    /** 编程式事务模板，保证扣金币、建订单、建奖励记录在同一事务内。 */
    private final TransactionTemplate transactionTemplate;

    /**
     * 购买虚拟商品入口，负责用户锁和事务编排。
     *
     * @param request 购买请求
     * @return 购买结果
     */
    public PurchaseResponse purchase(PurchaseRequest request) {
        Long userId = requireUserId(request.userId());
        if (request.productId() == null || request.productId() <= 0) {
            throw new BizException("productId 不能为空");
        }
        return userLockTemplate.execute(
                "purchase",
                userId,
                () -> transactionTemplate.execute(status -> doPurchase(userId, request.productId())));
    }

    /**
     * 在购买主事务内完成商品校验、金币扣减、订单创建和奖励记录创建。
     *
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @return 购买结果
     */
    private PurchaseResponse doPurchase(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.isEnabled()) {
            throw new BizException("商品不存在或已下架");
        }

        userAssetMapper.ensureUserAsset(userId);
        if (userAssetMapper.deductCoin(userId, product.getCoinPrice()) != 1) {
            throw new BizException("金币余额不足");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setCoinAmount(product.getCoinPrice());
        order.setStatus("SUCCESS");
        purchaseOrderMapper.insertOrder(order);
        RewardRecord rewardRecord = rewardRecordCommandService.createPending(
                userId,
                RewardBizType.PURCHASE,
                "order:" + order.getId(),
                0,
                product.getRewardPoint(),
                0,
                0);
        eventPublisher.publishEvent(new RewardSettlementEvent(rewardRecord.getId()));
        return new PurchaseResponse(order.getId(), rewardRecord.getId(), product.getCoinPrice());
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
