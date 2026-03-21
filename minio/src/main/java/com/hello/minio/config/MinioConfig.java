package com.hello.minio.config;

import io.minio.MinioClient;
import lombok.Data;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /** IP地址 */
    private String endpoint;

    /** 账户 */
    private String accessKey;

    /** 密码 */
    private String secretKey;

    /** 文件夹名称 */
    private String bucketName;

    @Bean
    public MinioClient minioClient() {

        /** 自定义配置 */
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();


        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .httpClient(httpClient)
            .build();
    }
}