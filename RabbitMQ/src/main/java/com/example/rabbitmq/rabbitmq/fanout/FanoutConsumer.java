package com.example.rabbitmq.rabbitmq.fanout;

import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 广播消费者
 */
@Component
public class FanoutConsumer {

    // 消费队列1
    @RabbitListener(queues = RabbitMqConfig.FANOUT_QUEUE_1)
    public void consume1(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("Fanout消费者1（库存系统）接收：" + msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    // 消费队列2
    @RabbitListener(queues = RabbitMqConfig.FANOUT_QUEUE_2)
    public void consume2(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("Fanout消费者2（日志系统）接收：" + msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
