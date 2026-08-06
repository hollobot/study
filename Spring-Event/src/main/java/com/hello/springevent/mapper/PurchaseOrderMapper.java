package com.hello.springevent.mapper;

import com.hello.springevent.domain.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderMapper {

    /**
     * 插入虚拟商品购买订单，并回填自增订单 ID。
     *
     * @param purchaseOrder 购买订单
     * @return 影响行数
     */
    int insertOrder(PurchaseOrder purchaseOrder);
}
