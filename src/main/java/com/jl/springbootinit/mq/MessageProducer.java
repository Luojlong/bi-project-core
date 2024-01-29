package com.jl.springbootinit.mq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.jl.springbootinit.mq.MqConstant.EXCHANGE_NAME;
import static com.jl.springbootinit.mq.MqConstant.ROUTING_KEY;

@Component
public class MessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message){
        MessageProperties messageProperties = new MessageProperties();
        // 设置超时时间为4分钟（240秒），其中模型处理时间为225s
        messageProperties.setExpiration("240000");
        Message msg = new Message(message.getBytes(), messageProperties);
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, msg);
    }

}
