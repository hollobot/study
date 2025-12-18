package com.example.rabbitmq.rabbitmq.simple;

import com.example.rabbitmq.config.RabbitMqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimpleProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String msg) {
        // 发送消息到简单队列
        rabbitTemplate.convertAndSend(RabbitMqConfig.SIMPLE_QUEUE, msg);
        System.out.println("简单队列生产者发送消息：" + msg);
    }
}