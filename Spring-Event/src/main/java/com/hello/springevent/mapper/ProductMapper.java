package com.hello.springevent.mapper;

import com.hello.springevent.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductMapper {

    /**
     * 根据商品 ID 查询虚拟商品。
     *
     * @param productId 商品 ID
     * @return 商品信息，不存在时返回 null
     */
    Product selectById(@Param("productId") Long productId);
}
