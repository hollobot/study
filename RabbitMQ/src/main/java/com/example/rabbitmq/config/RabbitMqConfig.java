package com.example.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    // 1. 简单队列（基础场景）
    public static final String SIMPLE_QUEUE = "simple.queue";
    // 2. 工作队列（负载均衡）
    public static final String WORK_QUEUE = "work.queue";
    // 3. 发布订阅（Fanout交换机）
    public static final String FANOUT_EXCHANGE = "fanout.exchange";
    public static final String FANOUT_QUEUE_1 = "fanout.queue1";
    public static final String FANOUT_QUEUE_2 = "fanout.queue2";
    // 4. 路由模式（Direct交换机）
    public static final String DIRECT_EXCHANGE = "direct.exchange";
    public static final String DIRECT_QUEUE_ORDER = "direct.queue.order";
    public static final String DIRECT_QUEUE_PAY = "direct.queue.pay";
    // 5. 主题模式（Topic交换机）
    public static final String TOPIC_EXCHANGE = "topic.exchange";
    public static final String TOPIC_QUEUE_USER = "topic.queue.user";
    // 6. 死信队列（异常消息处理）
    public static final String NORMAL_QUEUE = "normal.queue";
    public static final String DEAD_LETTER_EXCHANGE = "dlx.exchange";
    public static final String DEAD_LETTER_QUEUE = "dlx.queue";

    // ========== 简单队列 Bean ==========
    @Bean
    public Queue simpleQueue() {
        return QueueBuilder.durable(SIMPLE_QUEUE).build(); // 持久化队列
    }

    // ========== 工作队列 Bean ==========
    @Bean
    public Queue workQueue() {
        return QueueBuilder.durable(WORK_QUEUE).build();
    }

    // ========== 发布订阅（Fanout） ==========
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE, true, false);
    }
    @Bean
    public Queue fanoutQueue1() {
        return QueueBuilder.durable(FANOUT_QUEUE_1).build();
    }
    @Bean
    public Queue fanoutQueue2() {
        return QueueBuilder.durable(FANOUT_QUEUE_2).build();
    }
    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }

    // ========== 路由模式（Direct） ==========
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }
    @Bean
    public Queue directQueueOrder() {
        return QueueBuilder.durable(DIRECT_QUEUE_ORDER).build();
    }
    @Bean
    public Queue directQueuePay() {
        return QueueBuilder.durable(DIRECT_QUEUE_PAY).build();
    }
    @Bean
    public Binding directBindingOrder() {
        // 绑定路由键：order
        return BindingBuilder.bind(directQueueOrder()).to(directExchange()).with("order");
    }
    @Bean
    public Binding directBindingPay() {
        // 绑定路由键：pay
        return BindingBuilder.bind(directQueuePay()).to(directExchange()).with("pay");
    }

    // ========== 主题模式（Topic） ==========
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }
    @Bean
    public Queue topicQueueUser() {
        return QueueBuilder.durable(TOPIC_QUEUE_USER).build();
    }
    @Bean
    public Binding topicBindingUser() {
        // 路由键匹配：user.*（匹配user.add、user.update等）
        return BindingBuilder.bind(topicQueueUser()).to(topicExchange()).with("user.*");
    }

    // ========== 死信队列 ==========
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("dlx");
    }
    @Bean
    public Queue normalQueue() {
        // 普通队列绑定死信交换机 + 路由键
        return QueueBuilder.durable(NORMAL_QUEUE)
            .deadLetterExchange(DEAD_LETTER_EXCHANGE)
            .deadLetterRoutingKey("dlx")
            .ttl(10000) // 可选：消息10秒未消费则进入死信
            .build();
    }
}