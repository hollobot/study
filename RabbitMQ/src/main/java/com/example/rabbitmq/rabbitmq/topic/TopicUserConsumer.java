package com.example.rabbitmq.rabbitmq.topic;
import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class TopicUserConsumer {
    // 监听匹配 user.* 的队列（user.add、user.update、user.delete 都能匹配）
    @RabbitListener(queues = RabbitMqConfig.TOPIC_QUEUE_USER)
    public void consume(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("Topic消费者（用户模块）接收：" + msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}