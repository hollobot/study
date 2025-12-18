package com.example.rabbitmq.rabbitmq.topic;
import com.example.rabbitmq.config.RabbitMqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TopicProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendUserMessage(String action, String msg) {
        // 路由键格式：user.操作（如 user.add、user.update）
        String routingKey = "user." + action;
        rabbitTemplate.convertAndSend(RabbitMqConfig.TOPIC_EXCHANGE, routingKey, msg);
        System.out.println("Topic生产者发送（路由键：" + routingKey + "）：" + msg);
    }
}