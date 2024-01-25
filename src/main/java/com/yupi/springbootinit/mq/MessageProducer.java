package com.yupi.springbootinit.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.yupi.springbootinit.mq.MqConstant.EXCHANGE_NAME;
import static com.yupi.springbootinit.mq.MqConstant.ROUTING_KEY;

@Component
public class MessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);

    }

}
