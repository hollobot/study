package com.hello.langchain4j.utils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义分割器
 */
@Slf4j
@Component
public class QaBlockSplitter implements DocumentSplitter {

    @Override
    public List<TextSegment> split(Document document) {
        // 1. 防御：文档为空直接返回
        if (document == null) {
            return new ArrayList<>();
        }

        String content = document.text();
        // 2. 防御：文本为空/空白直接返回
        if (content == null || content.isBlank()) {
            return new ArrayList<>();
        }

        List<TextSegment> segments = new ArrayList<>();
        // 按所有换行分割（支持Windows/Linux/Mac）
        String[] lines = content.split("\\R");

        for (String line : lines) {
            // 3. 核心：清理空格 + 严格判断非空
            String finalText = line.trim();
            // 只有真正有内容的文本，才创建切片
            if (!finalText.isBlank()) {
                segments.add(TextSegment.from(finalText));
            }
        }

        return segments;
    }

    @Override
    public List<TextSegment> splitAll(List<Document> documents) {
        return documents.stream()
            .flatMap(doc -> split(doc).stream())
            .collect(Collectors.toList());
    }
}