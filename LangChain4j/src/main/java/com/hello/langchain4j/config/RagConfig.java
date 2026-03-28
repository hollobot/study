package com.hello.langchain4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RagConfig {

    /**
     * ContentRetriever：定义"如何从向量库检索内容"
     *
     * maxResults = 5：每次检索返回最相似的 5 个文档片段
     * minScore = 0.8：相似度低于 0.8 的片段不返回（0~1之间，越高越严格）
     *
     * 内部工作流程：
     * 1. 拿到用户问题
     * 2. 调用 embeddingModel 将问题转成向量
     * 3. 在 embeddingStore（ES）中做 KNN 检索
     * 4. 返回最相似的 N 个文本片段
     */
    @Bean
    public ContentRetriever contentRetriever(
        @Qualifier("elasticsearchEmbeddingStore") EmbeddingStore<TextSegment> embeddingStore,
        @Qualifier("bGEM3") EmbeddingModel embeddingModel) {

        return EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore) // es向量存储
            .embeddingModel(embeddingModel) // 向量模型
            .maxResults(5)
            .minScore(0.8)
            .build();
    }

}