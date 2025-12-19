package com.example.rabbitmq.rabbitmq.quorum;

import com.example.rabbitmq.config.QuorumQueueConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// 消费者
@Component
@Slf4j
public class QuorumConsumer {
    @RabbitListener(queues = QuorumQueueConfig.QUORUM_QUEUE)
    public void consume(String msg, Channel channel, Message message) throws Exception {
        try {
            System.out.println("仲裁队列消费：" + msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); // 手动确认：要确认的消息序号、是否批量确认
        } catch (Exception e) {
            log.error("消息消费失败：{}", e.getMessage());
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true); // 失败重试 ：要确认的消息序号、是否批量确认、是否重新入队
        }
    }
}