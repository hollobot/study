package com.example.rabbitmq.rabbitmq.quorum;

import com.example.rabbitmq.config.QuorumQueueConfig;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 生产者
@Component
public class QuorumProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMsg(String msg) {
        rabbitTemplate.convertAndSend(QuorumQueueConfig.QUORUM_QUEUE, msg, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 消息持久化
            return message;
        });
    }
}