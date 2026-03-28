package com.hello.langchain4j.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
public class StreamingChatService {

    @Qualifier("openAiStreamingModel")
    @Autowired
    private StreamingChatLanguageModel openAiStreamingModel;

    @Qualifier("zhipuStreamingModel")
    @Autowired
    private StreamingChatLanguageModel zhipuStreamingModel;

    @Qualifier("ollamaStreamingModel")
    @Autowired
    private StreamingChatLanguageModel ollamaStreamingModel;

    public Flux<String> streamWithOpenAi(String prompt) {
        return buildFlux(openAiStreamingModel, prompt);
    }

    public Flux<String> streamWithZhipu(String prompt) {
        return buildFlux(zhipuStreamingModel, prompt);
    }

    public Flux<String> streamWithOllama(String prompt) {
        return buildFlux(ollamaStreamingModel, prompt);
    }

    /**
     * 本地模型流式输出服务
     * @param systemMessage 系统提示词
     * @param userMessage 用户提示词
     * @return 流式对象
     */
    public Flux<String> streamWithOllama(SystemMessage systemMessage, UserMessage userMessage) {
        return buildFlux(ollamaStreamingModel, systemMessage, userMessage);
    }

    /**
     * buildFlux
     *
     * @param model 模型
     * @param userMessage 用户提示词
     * @return
     */
    private Flux<String> buildFlux(StreamingChatLanguageModel model, String userMessage) {
        // Sinks 是 WebFlux 的响应式"管道"，把回调转成 Flux
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        model.generate(userMessage, getStreamingResponseHandler(sink));

        return sink.asFlux();
    }

    /**
     * buildFlux
     *
     * @param model 模型
     * @param systemMessage 系统提示词
     * @param userMessage 用户提示词
     * @return
     */
    private Flux<String> buildFlux(StreamingChatLanguageModel model, SystemMessage systemMessage, UserMessage userMessage) {
        // Sinks 是 WebFlux 的响应式"管道"，把回调转成 Flux
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        model.generate(List.of(systemMessage, userMessage), getStreamingResponseHandler(sink));

        return sink.asFlux();
    }

    /**
     * 构建 StreamingResponseHandler 对象 （作用与 model.generate() 入参）
     *
     * @param sink WebFlux 的响应式"管道"，把回调转成 Flux （流式输出的对象）
     * @return
     */
    public StreamingResponseHandler<AiMessage> getStreamingResponseHandler(Sinks.Many<String> sink) {
        return new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                // 每来一个 token，往管道里推一个
                sink.tryEmitNext(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                // 生成完毕，关闭管道
                sink.tryEmitComplete();
            }

            @Override
            public void onError(Throwable error) {
                // 发生错误，向管道发送错误信号
                sink.tryEmitError(error);
            }
        };
    }
}