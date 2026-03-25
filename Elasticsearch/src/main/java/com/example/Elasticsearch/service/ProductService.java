package com.example.Elasticsearch.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.Elasticsearch.Repository.ProductRepository;
import com.example.Elasticsearch.pojo.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // 新增 / 修改
    public Product save(Product product) {
        return productRepository.save(product);
    }

    // 批量新增
    public List<Product> batchSave(List<Product> products) {
        Iterable<Product> all = productRepository.saveAll(products);
        List<Product> list = new ArrayList<>();
        all.forEach(list::add);
        return list;
    }

    // 根据 ID 查询
    public Product getById(String id) {
        return productRepository.findById(id).orElse(null);
    }

    // 查询全部
    public List<Product> getAll() {
        Iterable<Product> all = productRepository.findAll();
        List<Product> list = new ArrayList<>();
        all.forEach(list::add);
        return list;
    }

    // 按名称模糊搜索
    public List<Product> searchByName(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    // 按分类查询
    public List<Product> searchByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // 按价格范围查询
    public List<Product> searchByPriceRange(Double min, Double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // 多字段全文搜索
    public List<Product> fullTextSearch(String keyword) {
        /**
         * | 字段 | 权重 | 含义 |
         * |---|---|---|
         * | `name^3` | 3 | 商品名称匹配到，得分 × 3 |
         */
        Query multiMatchQuery = Query.of(q -> q.multiMatch(mm -> mm.query(keyword).fields("name^3", "description^1")));
        NativeQuery nativeQuery = NativeQuery.builder().withQuery(multiMatchQuery).build();
        return elasticsearchOperations.search(nativeQuery, Product.class).stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    // 删除
    public void delete(String id) {
        productRepository.deleteById(id);
    }
}