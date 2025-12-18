package com.example.rabbitmq.rabbitmq.normal;
import com.example.rabbitmq.config.RabbitMqConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NormalProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendNormalMsg(String msg) {
        // 发送到普通队列（超时/消费失败会进入死信）
        rabbitTemplate.convertAndSend(RabbitMqConfig.NORMAL_QUEUE, msg);
        System.out.println("普通队列生产者发送：" + msg);
    }
}