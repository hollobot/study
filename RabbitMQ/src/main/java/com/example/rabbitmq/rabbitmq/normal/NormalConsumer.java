package com.example.rabbitmq.rabbitmq.normal;
import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NormalConsumer {
    @RabbitListener(queues = RabbitMqConfig.NORMAL_QUEUE)
    public void consume(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("普通队列消费者接收：" + msg);
            // 故意抛出异常，让消息进入死信
            throw new RuntimeException("消费失败，进入死信");
        } catch (Exception e) {
            // requeue=false：拒绝并进入死信
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}