package com.example.rabbitmq.rabbitmq.normal;
import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class DeadLetterConsumer {
    @RabbitListener(queues = RabbitMqConfig.DEAD_LETTER_QUEUE)
    public void consume(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("死信队列消费者接收（人工排查）：" + msg);
            // 死信消息处理（如记录日志、人工通知）
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 死信消费失败：直接丢弃（或持久化到数据库）
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}