package com.hello.langchain4j.config;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    /** 索引名称 */
    @Value("${elasticsearch.index-name}")
    private String indexName;

    /**
     * 构建 ES 低层 RestClient（本地无认证，使用 http）
     */
    @Bean
    public RestClient restClient() {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(username, password)
        );

        return RestClient.builder(new HttpHost(host, port, "http"))
            .setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            )
            .build();
    }


    /**
     * 构建 LangChain4j 的向量存储
     * ElasticsearchEmbeddingStore 封装了向量写入/检索的逻辑
     * restClient es客户端
     * indexName 索引名称
     */
    @Bean(name = "elasticsearchEmbeddingStore")
    public ElasticsearchEmbeddingStore embeddingStore(RestClient restClient) {
        return ElasticsearchEmbeddingStore.builder()
            .restClient(restClient)
            .indexName(indexName)
            .build();
    }

}