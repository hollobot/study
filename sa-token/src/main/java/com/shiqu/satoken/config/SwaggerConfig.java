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
                .title("sa-token 接口文档")
                .version("1.0")
                .description("Sa-Token 是一个轻量级 Java 权限认证框架，主要解决：登录认证、权限认证、单点登录、OAuth2.0、分布式Session会话、微服务网关鉴权 等一系列权限相关问题")
                .contact(new Contact() // 联系人信息（可选）
                    .name("hello")
                    .email("dev@example.com")));
    }
}