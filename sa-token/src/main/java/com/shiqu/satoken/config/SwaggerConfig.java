package com.shiqu.satoken.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    /**
     * 配置 OpenAPI 文档信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            // 文档基本信息
            .info(new Info()
                .title("RabbitMQ 消息发送接口文档")
                .version("1.0")
                .description("基于 Spring Boot + RabbitMQ 的消息发送接口，支持简单队列、工作队列、发布订阅等场景")
                .contact(new Contact() // 联系人信息（可选）
                    .name("hello")
                    .email("dev@example.com")));
    }
}