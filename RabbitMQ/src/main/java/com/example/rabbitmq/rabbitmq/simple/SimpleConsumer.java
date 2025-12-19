package com.example.rabbitmq.rabbitmq.simple;

import com.example.rabbitmq.config.RabbitMqConfig;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * 简单队列消费者
 */
@Component
public class SimpleConsumer {
    // 监听简单队列
    @RabbitListener(queues = RabbitMqConfig.SIMPLE_QUEUE)
    public void consume(String msg, Channel channel, Message message) throws IOException {
        try {
            // 1. 业务处理
            System.out.println("简单队列消费者接收消息：" + msg);
            // 模拟业务逻辑
            if (msg.contains("error")) {
                throw new RuntimeException("业务处理失败");
            }

            // 2. 手动确认消息（关键：告知MQ消息已消费成功）
            // 第二个参数：multiple（是否批量确认）
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 3. 消费失败：拒绝消息并重新入队（或直接丢弃/进入死信）
            // requeue=true：重新入队；requeue=false：丢弃（或进入死信）
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            System.err.println("消费失败：" + e.getMessage());
        }
    }
}