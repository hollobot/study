package com.hello.springevent.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    /**
     * 创建 Redisson 客户端，用于分布式锁和 Redis Bitmap。
     *
     * @param host Redis 地址
     * @param port Redis 端口
     * @param password Redis 密码，空字符串表示无密码
     * @return Redisson 客户端
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            @Value("${redis.host}") String host,
            @Value("${redis.port}") int port,
            @Value("${redis.password:}") String password) {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        config.useSingleServer().setAddress(address);
        if (password != null && !password.isBlank()) {
            config.useSingleServer().setPassword(password);
        }
        return Redisson.create(config);
    }
}
