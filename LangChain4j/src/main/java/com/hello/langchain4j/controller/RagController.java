package com.hello.langchain4j.controller;

import com.hello.langchain4j.service.KnowledgeBaseService;
import com.hello.langchain4j.service.ManualRagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/rag")
@Tag(name = "RAG 知识库", description = "知识库导入与对话接口")
@RequiredArgsConstructor
public class RagController {

    /** RAG对话服务 */
    private final ManualRagService manualRagService;

    /** 向量知识库存储 */
    private final KnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "流式对话", description = "基于知识库进行流式问答")
    @GetMapping(value = "/chat", produces = "text/plain;charset=UTF-8")
    public Flux<String> chat(@Parameter(description = "用户问题", required = true) @RequestParam String question) {
        return manualRagService.chat(question);
    }

    @Operation(summary = "导入 MD 文档", description = "上传 Markdown 文件导入到向量知识库")
    @PostMapping("/ingest/markdown")
    public Map<String, String> ingestMarkdown(
        @Parameter(description = "Markdown 文件（.md）", required = true)
        @RequestParam("file") MultipartFile file,
        @RequestParam("isCover") Boolean isCover) throws IOException {

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".md")) {
            return Map.of("status", "error", "message", "只支持 .md 文件");
        }

        knowledgeBaseService.ingestMarkdownFile(file,isCover);

        return Map.of("status", "success", "message", originalFilename + " 导入完成");
    }

    @Operation(summary = "查询知识库所有数据", description = "分页查询 Elasticsearch 中的向量数据")
    @GetMapping("/list")
    public Map<String, Object> list(
        @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "索引") @RequestParam(defaultValue = "rag_knowledge_base") String indexName) {
        return knowledgeBaseService.listAll(page, size, indexName);
    }
}