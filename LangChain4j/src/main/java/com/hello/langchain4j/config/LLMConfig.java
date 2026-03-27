package com.hello.langchain4j.config;

import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.community.model.zhipu.chat.ChatCompletionModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LLMConfig {

    @Value("${openai.api-key}")
    private String openaiKey;

    @Value("${zhipu.api-key}")
    private String zhipuKey;



    // OpenAI 流式模型
    @Bean(name = "openAiStreamingModel")
    public StreamingChatLanguageModel openAiStreamingModel() {
        return OpenAiStreamingChatModel.builder()
            .apiKey(openaiKey)
            .modelName("gpt-3.5-turbo")
            .temperature(0.7)
            .logRequests(true)
            .build();
    }

    // 智谱AI 流式模型
    @Bean(name = "zhipuStreamingModel")
    public StreamingChatLanguageModel zhipuStreamingModel() {
        return ZhipuAiStreamingChatModel.builder()
            .apiKey(zhipuKey)
            .model(ChatCompletionModel.GLM_4_FLASH)
            .temperature(0.9)
            .maxToken(99999)
            .callTimeout(Duration.ofSeconds(60))
            .connectTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(60))
            .logRequests(true)
            .build();
    }

    // 智谱AI 流式模型
    @Bean(name = "glmModel")
    public ChatLanguageModel glmModel() {
        return ZhipuAiChatModel.builder()
            .apiKey(zhipuKey)
            .model(ChatCompletionModel.GLM_4_FLASH)
            .temperature(0.9)
            .maxToken(99999)
            .callTimeout(Duration.ofSeconds(60))
            .connectTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(60))
            .logRequests(true)
            .build();
    }

    static String MODEL_NAME = "deepseek-r1:1.5b"; // try other local ollama model names
    static String BASE_URL = "http://192.168.30.130:11434"; // local ollama base url


    @Bean(name = "ollamaStreamingModel")
    public StreamingChatLanguageModel OllamaStreamingModel() {
        return OllamaStreamingChatModel.builder()
            .baseUrl(BASE_URL)
            .modelName(MODEL_NAME)
            .temperature(0.0)
            .logRequests(true)
            .build();
    }
}