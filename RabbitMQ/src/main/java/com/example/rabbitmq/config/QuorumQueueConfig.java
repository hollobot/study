package com.example.rabbitmq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class QuorumQueueConfig {

    public static final String QUORUM_QUEUE = "quorum.queue";

    @Bean
    public Queue quorumQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum"); // 核心：指定为仲裁队列
        // 可选：设置副本数（默认3）
        args.put("x-quorum-initial-group-size", 3);
        return new Queue(QUORUM_QUEUE, true, false, false, args);
    }
}