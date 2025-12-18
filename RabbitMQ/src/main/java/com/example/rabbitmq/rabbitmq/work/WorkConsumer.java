package com.example.rabbitmq.rabbitmq.work;

import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WorkConsumer {


    // 消费者1
    @RabbitListener(queues = RabbitMqConfig.WORK_QUEUE)
    public void consume1(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("消费者1 接收：" + msg);
            // 模拟处理耗时（比如处理奇数消息慢）
            if (Integer.parseInt(msg.split("-")[1]) % 2 != 0) {
                Thread.sleep(2000);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    public class WorkConsumer1 {

    }

    // 消费者2
    @RabbitListener(queues = RabbitMqConfig.WORK_QUEUE)
    public void consume2(String msg, Channel channel, Message message) throws IOException {
        try {
            System.out.println("消费者2 接收：" + msg);
            // 模拟处理耗时（偶数消息慢）
            if (Integer.parseInt(msg.split("-")[1]) % 2 == 0) {
                Thread.sleep(2000);
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }
}
