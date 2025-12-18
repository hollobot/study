package com.example.rabbitmq.rabbitmq.direct;

import com.example.rabbitmq.config.RabbitMqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DirectProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendByRoutingKey(String routingKey, String msg) {
        // 发送消息时指定路由键
        rabbitTemplate.convertAndSend(RabbitMqConfig.DIRECT_EXCHANGE, routingKey, msg);
        System.out.println("Direct生产者发送（路由键：" + routingKey + "）：" + msg);
    }
}