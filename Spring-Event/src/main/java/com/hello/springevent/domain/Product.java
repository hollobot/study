package com.hello.springevent.domain;

import lombok.Data;

@Data
public class Product {

    /** 商品 ID。 */
    private Long id;

    /** 商品名称。 */
    private String productName;

    /** 商品金币价格。 */
    private int coinPrice;

    /** 购买商品后奖励的积分。 */
    private int rewardPoint;

    /** 商品是否上架。 */
    private boolean enabled;

}
