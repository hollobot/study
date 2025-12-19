package com.example.rabbitmq.controller;

import com.example.rabbitmq.rabbitmq.direct.DirectProducer;
import com.example.rabbitmq.rabbitmq.fanout.FanoutProducer;
import com.example.rabbitmq.rabbitmq.normal.NormalProducer;
import com.example.rabbitmq.rabbitmq.quorum.QuorumProducer;
import com.example.rabbitmq.rabbitmq.simple.SimpleProducer;
import com.example.rabbitmq.rabbitmq.topic.TopicProducer;
import com.example.rabbitmq.rabbitmq.work.WorkProducer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * RabbitMQ 消息发送接口
 */
@RestController
@RequestMapping("/rabbitmq")
@RequiredArgsConstructor // 替代 @Resource，Lombok 自动注入
@Tag(name = "RabbitMQ 消息接口", description = "各种消费场景的消息发送接口")
public class RabbitMqController {

    private final SimpleProducer simpleProducer;
    private final WorkProducer workProducer;
    private final FanoutProducer fanoutProducer;
    private final DirectProducer directProducer;
    private final TopicProducer topicProducer;
    private final NormalProducer normalProducer;
    private final QuorumProducer quorumProducer;

    /**
     * 简单队列 - 发送消息
     */
    @GetMapping("/simple/{msg}")
    @Operation(summary = "简单队列发送消息", description = "一对一消费场景，如短信通知、日志记录")
    public String sendSimpleMsg(@Parameter(description = "消息内容", required = true) @PathVariable String msg) {
        simpleProducer.sendMessage(msg);
        return "简单队列消息发送成功：" + msg;
    }

    /**
     * 工作队列 - 批量发送消息
     */
    @GetMapping("/work/{count}")
    @Operation(summary = "工作队列批量发送", description = "一对多负载均衡消费，如订单批量处理")
    public String sendWorkMsg(@Parameter(description = "消息数量", required = true, example = "10") @PathVariable Integer count) {
        workProducer.sendMessages(count);
        return "工作队列已发送 " + count + " 条消息";
    }

    /**
     * 发布订阅（Fanout）- 广播消息
     */
    @GetMapping("/fanout/{msg}")
    @Operation(summary = "发布订阅广播消息", description = "所有绑定队列都能接收，如多系统同步")
    public String sendFanoutMsg(@Parameter(description = "广播消息内容", required = true) @PathVariable String msg) {
        fanoutProducer.sendBroadcast(msg);
        return "Fanout 广播消息发送成功：" + msg;
    }

    /**
     * 路由模式（Direct）- 按路由键发送
     */
    @GetMapping("/direct/{routingKey}/{msg}")
    @Operation(summary = "Direct 精准路由", description = "按路由键分发消息，如订单/支付消息分离")
    public String sendDirectMsg(@Parameter(description = "路由键（order/pay）", required = true, example = "order") @PathVariable String routingKey,
        @Parameter(description = "消息内容", required = true) @PathVariable String msg) {
        directProducer.sendByRoutingKey(routingKey, msg);
        return "Direct 消息发送成功（路由键：" + routingKey + "）：" + msg;
    }

    /**
     * 主题模式（Topic）- 模糊路由发送
     */
    @GetMapping("/topic/{action}/{msg}")
    @Operation(summary = "Topic 模糊路由", description = "通配符匹配路由键，如 user.add/user.update")
    public String sendTopicMsg(@Parameter(description = "用户操作（user.add/user.update/user.delete）", required = true, example = "user.add") @PathVariable String action,
        @Parameter(description = "消息内容", required = true) @PathVariable String msg) {
        topicProducer.sendUserMessage(action, msg);
        return "Topic 消息发送成功（路由键：user." + action + "）：" + msg;
    }

    /**
     * 死信队列 - 发送普通消息（会进入死信）
     */
    @GetMapping("/dlx/{msg}")
    @Operation(summary = "死信队列测试", description = "消费失败/超时后进入死信队列，用于异常兜底")
    public String sendDlxMsg(@Parameter(description = "普通消息内容", required = true) @PathVariable String msg) {
        normalProducer.sendNormalMsg(msg);
        return "普通队列消息发送成功（将进入死信）：" + msg;
    }

    /**
     * 仲裁队列
     */
    @GetMapping("/quorum/{msg}")
    @Operation(summary = "仲裁队列测试", description = "一对一消费场景 仲裁队列测试")
    public String sendQuorumMsg(@Parameter(description = "普通消息内容", required = true) @PathVariable String msg) {
        quorumProducer.sendMsg(msg);
        return "仲裁队列消息发送成功：" + msg;
    }
}