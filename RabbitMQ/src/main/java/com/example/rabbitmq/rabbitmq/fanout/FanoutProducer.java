package com.example.rabbitmq.rabbitmq.fanout;

import com.example.rabbitmq.config.RabbitMqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class FanoutProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendBroadcast(String msg) {
        // 发送到Fanout交换机（无需路由键，路由键会被忽略）
        rabbitTemplate.convertAndSend(RabbitMqConfig.FANOUT_EXCHANGE, "", "广播消息：" + msg);
        System.out.println("Fanout生产者发送广播消息：" + msg);
    }
}