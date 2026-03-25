package com.example.Elasticsearch.pojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Schema(description = "商品实体")
@Document(indexName = "products")
public class Product {

    @Id
    @Schema(description = "商品ID")
    private String id;

    @Schema(description = "商品名称")
    @Field(type = FieldType.Text, analyzer = "ik_max_word") // 中文分词器（需安装IK）
    private String name;

    @Schema(description = "商品描述")
    @Field(type = FieldType.Text)
    private String description;

    @Schema(description = "商品价格")
    @Field(type = FieldType.Double)
    private Double price;

    @Schema(description = "商品分类")
    @Field(type = FieldType.Keyword)
    private String category;
}