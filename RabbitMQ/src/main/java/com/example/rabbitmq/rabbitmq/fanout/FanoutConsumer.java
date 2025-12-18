package com.example.rabbitmq.rabbitmq.fanout;

import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FanoutConsumer {

    // 消费队列1
    public class FanoutConsumer1 {
        @RabbitListener(queues = RabbitMqConfig.FANOUT_QUEUE_1)
        public void consume(String msg, Channel channel, Message message) throws IOException {
            try {
                System.out.println("Fanout消费者1（库存系统）接收：" + msg);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e) {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }
        }
    }

    // 消费队列2
    public class FanoutConsumer2 {
        @RabbitListener(queues = RabbitMqConfig.FANOUT_QUEUE_2)
        public void consume(String msg, Channel channel, Message message) throws IOException {
            try {
                System.out.println("Fanout消费者2（日志系统）接收：" + msg);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e) {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }
        }
    }
}
