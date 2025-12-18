package com.example.rabbitmq.rabbitmq.work;

import com.example.rabbitmq.config.RabbitMqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
public class WorkProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessages(int count) {
        // 发送多条消息到工作队列
        for (int i = 1; i <= count; i++) {
            String msg = "工作队列消息-" + i;
            rabbitTemplate.convertAndSend(RabbitMqConfig.WORK_QUEUE, msg);
            System.out.println("工作队列生产者发送：" + msg);
        }
    }
}