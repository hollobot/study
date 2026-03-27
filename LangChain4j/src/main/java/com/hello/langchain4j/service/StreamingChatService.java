package com.hello.langchain4j.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

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

    private Flux<String> buildFlux(StreamingChatLanguageModel model, String prompt) {
        // Sinks 是 WebFlux 的响应式"管道"，把回调转成 Flux
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        model.generate(prompt, new StreamingResponseHandler<AiMessage>() {
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
        });

        return sink.asFlux();
    }
}