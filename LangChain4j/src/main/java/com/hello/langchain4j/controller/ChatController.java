package com.hello.langchain4j.controller;

import com.hello.langchain4j.service.StreamingChatService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "AI 对话", description = "多模型对话接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final StreamingChatService streamingChatService;

    @Autowired
    @Qualifier("glmModel")
    private ChatLanguageModel glmModel;

    @Operation(summary = "OpenAI 对话", description = "使用 OpenAI 模型进行对话")
    @GetMapping(value = "/openAi", produces = "text/plain;charset=UTF-8")
    public Flux<String> openAiChat(
        @Parameter(description = "用户输入的消息", example = "你是谁？") @RequestParam(defaultValue = "你是谁？") String message) {
        return streamingChatService.streamWithOpenAi(message);
    }

    @Operation(summary = "GLM 流式对话", description = "使用 质谱 模型进行对话")
    @GetMapping(value = "/stream/glm", produces = "text/plain;charset=UTF-8")
    public Flux<String> glmStreamChat(
        @Parameter(description = "用户输入的消息", example = "你是谁？") @RequestParam(defaultValue = "你是谁？") String message) {
        return streamingChatService.streamWithZhipu(message);
    }

    @Operation(summary = "GLM 对话", description = "使用 质谱 模型进行对话")
    @GetMapping(value = "/glm")
    public String glmChat(
        @Parameter(description = "用户输入的消息", example = "你是谁？") @RequestParam(defaultValue = "你是谁？") String message) {
        return glmModel.generate(message);
    }

    @Operation(summary = "ollama 对话", description = "ollama 本地模型进行对话")
    @GetMapping(value = "/ollama", produces = "text/plain;charset=UTF-8")
    public Flux<String> ollamaChat(
        @Parameter(description = "用户输入的消息", example = "你是谁？") @RequestParam(defaultValue = "你是谁？") String message) {
        return streamingChatService.streamWithOllama(message);
    }
}