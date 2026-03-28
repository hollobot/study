package com.hello.langchain4j.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hello.langchain4j.utils.QaBlockSplitter;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 知识库向量存储 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    /** 向量数据库存储 */
    private final EmbeddingStore<TextSegment> embeddingStore;

    /** 向量模型 */
    @Autowired
    @Qualifier("bGEM3")
    private EmbeddingModel embeddingModel;

    /** 自定义分割器 */
    private final QaBlockSplitter qaBlockSplitter;

    /** es客户端 */
    private final ElasticsearchClient elasticsearchClient;

    /**
     * 获取md文档对象，存入向量数据库
     * @param file 文件
     * @param isCover 是否覆盖
     */
    public void ingestMarkdownFile(MultipartFile file,Boolean isCover) throws IOException {

        if(isCover!=null && isCover){
            embeddingStore.removeAll();
        }

        // 直接从流读取内容，不需要写临时文件
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        Document document = Document.from(content);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(qaBlockSplitter)
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();

        ingestor.ingest(document);
        log.info("MD 文档导入完成：{}", file.getOriginalFilename());
    }


    public Map<String, Object> listAll(int page, int size,String indexName) {
        try {
            // 构建分页查询，match_all 查所有数据
            SearchResponse<ObjectNode> response = elasticsearchClient.search(s -> s
                    .index(indexName)
                    .from((page - 1) * size)   // 从第几条开始
                    .size(size)                 // 取几条
                    .query(q -> q.matchAll(m -> m)),  // 查全部
                ObjectNode.class
            );

            // 提取 hits 里的数据
            List<Map<String, Object>> records = response.hits().hits().stream()
                .map(hit -> {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", hit.id());

                    if (hit.source() != null) {
                        // 文本内容
                        JsonNode textNode = hit.source().get("text");
                        record.put("text", textNode != null ? textNode.asText() : "");

                        // 向量数据
                        JsonNode vectorNode = hit.source().get("vector");
                        if (vectorNode != null && vectorNode.isArray()) {
                            List<Double> vector = new ArrayList<>();
                            vectorNode.forEach(v -> vector.add(v.asDouble()));
                            record.put("vector", vector);
                            record.put("vectorDimension", vector.size()); // 顺便返回维度
                        }

                        // metadata
                        JsonNode metadataNode = hit.source().get("metadata");
                        record.put("metadata", metadataNode != null ? metadataNode : Map.of());
                    }
                    return record;
                })
                .collect(Collectors.toList());

            long total = response.hits().total() != null
                ? response.hits().total().value() : 0;

            return Map.of(
                "total", total,
                "page", page,
                "size", size,
                "records", records
            );

        } catch (IOException e) {
            log.error("查询 ES 数据失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }
}