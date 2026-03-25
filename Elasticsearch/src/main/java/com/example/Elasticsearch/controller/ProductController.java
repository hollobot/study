package com.example.Elasticsearch.controller;

import com.example.Elasticsearch.pojo.Product;
import com.example.Elasticsearch.service.ProductService;
import com.example.Elasticsearch.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品搜索", description = "Elasticsearch 商品管理接口")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "新增商品")
    @PostMapping("/save")
    public Result<Product> save(@RequestBody Product product) {
        return Result.success(productService.save(product));
    }

    @Operation(summary = "批量新增")
    @PostMapping("/batchSave")
    public Result<List<Product>> batchSave(@RequestBody List<Product> products) {
        return Result.success(productService.batchSave(products));
    }

    @Operation(summary = "根据ID查询商品")
    @GetMapping("/{id}")
    public Result<Product> getById(@Parameter(description = "商品ID") @PathVariable String id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "查询全部商品")
    @GetMapping
    public Result<List<Product>> getAll() {
        return Result.success(productService.getAll());
    }

    @Operation(summary = "关键词全文搜索（多字段）")
    @GetMapping("/search")
    public Result<List<Product>> search(@Parameter(description = "搜索关键词") @RequestParam String keyword) {
        return Result.success(productService.fullTextSearch(keyword));
    }

    @Operation(summary = "按名称模糊搜索")
    @GetMapping("/search/name")
    public Result<List<Product>> searchByName(@Parameter(description = "商品名称关键词") @RequestParam String keyword) {
        return Result.success(productService.searchByName(keyword));
    }

    @Operation(summary = "按分类查询")
    @GetMapping("/search/category")
    public Result<List<Product>> searchByCategory(@Parameter(description = "商品分类") @RequestParam String category) {
        return Result.success(productService.searchByCategory(category));
    }

    @Operation(summary = "按价格范围查询")
    @GetMapping("/search/price")
    public Result<List<Product>> searchByPrice(@Parameter(description = "最低价格") @RequestParam Double min,
        @Parameter(description = "最高价格") @RequestParam Double max) {
        return Result.success(productService.searchByPriceRange(min, max));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "商品ID") @PathVariable String id) {
        productService.delete(id);
        return Result.success();
    }
}