package com.hello.springevent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * 配置 OpenAPI 文档基础信息。
     *
     * @return OpenAPI 文档配置
     */
    @Bean
    public OpenAPI shiquAiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("适趣 AI 中文 - 倒霉狗后端接口")
                        .version("1.0.0")
                        .description("复现简历中 Spring Event、Redisson、Redis Bitmap、奖励补偿相关接口。"));
    }
}
