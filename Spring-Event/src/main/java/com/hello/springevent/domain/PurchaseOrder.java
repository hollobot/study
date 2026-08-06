package com.hello.springevent.domain;

import lombok.Data;

@Data
public class PurchaseOrder {

    /** 订单 ID。 */
    private Long id;

    /** 用户 ID。 */
    private Long userId;

    /** 商品 ID。 */
    private Long productId;

    /** 本次支付金币数量。 */
    private int coinAmount;

    /** 订单状态。 */
    private String status;

}
