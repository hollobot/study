package com.example.rabbitmq;

import com.example.rabbitmq.rabbitmq.simple.SimpleProducer;
import com.example.rabbitmq.rabbitmq.work.WorkProducer;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RabbitMqApplicationTests {
    @Resource
    private SimpleProducer simpleProducer;

    @Resource
    private WorkProducer workProducer;
/*    private final FanoutProducer fanoutProducer;
    private final DirectProducer directProducer;
    private final TopicProducer topicProducer;
    private final NormalProducer normalProducer;*/

    // 测试简单队列
    @Test
    public void testSimple() {
        simpleProducer.sendMessage("Hello RabbitMQ!");
    }

    // 测试工作队列
    @Test
    public void testWork() {
        workProducer.sendMessages(10);
    }

}