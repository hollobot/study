package com.example.rabbitmq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建StreamQueueConfig类，用于配置RabbitMQ的Stream队列。
 */
@Configuration
public class StreamQueueConfig {
    public static final String STREAM_QUEUE = "stream.queue";

    /** 创建 stream 队列 */
    public Queue streamQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "stream"); // 核心：指定为流式队列
        args.put("x-max-length-bytes", 20_000_000_000L); // 最大存储20GB
        args.put("x-stream-max-segment-size-bytes", 536_870_912L); // 每个日志段512MB
        return new Queue(STREAM_QUEUE, true, false, false, args);
    }
}
