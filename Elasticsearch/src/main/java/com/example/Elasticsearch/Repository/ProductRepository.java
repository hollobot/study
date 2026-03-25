package com.example.Elasticsearch.Repository;

import com.example.Elasticsearch.pojo.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    // 按名称模糊搜索（Spring Data 自动实现）
    // findByNameContaining 查询name  Containing-包含
    List<Product> findByNameContaining(String name);

    // 按分类查询
    List<Product> findByCategory(String category);

    // 按价格范围
    List<Product> findByPriceBetween(Double min, Double max);
}