package com.example.rabbitmq.rabbitmq.direct;

import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class DirectConsumer {

    // 消费订单消息（路由键：order）
    @RabbitListener(queues = RabbitMqConfig.DIRECT_QUEUE_ORDER)
    public void consume1(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("Direct消费者（订单）接收：" + msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    // 消费支付消息（路由键：pay）
    @RabbitListener(queues = RabbitMqConfig.DIRECT_QUEUE_PAY)
    public void consume2(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("Direct消费者（支付）接收：" + msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
