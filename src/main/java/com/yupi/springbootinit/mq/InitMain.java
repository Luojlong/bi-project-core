package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.yupi.springbootinit.mq.MqConstant.*;

public class InitMain {
    @SneakyThrows
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // 声明死信队列及交换机
            channel.exchangeDeclare(DEAD_EXCHANGE, "direct");
            channel.queueDeclare(DEAD_QUEUE_NAME, true, false, false, null);
            channel.queueBind(DEAD_QUEUE_NAME, DEAD_EXCHANGE, DEAD_ROUTING_KEY);

            // 声明正常队列添加死信队列相关设置
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-dead-letter-exchange", DEAD_EXCHANGE);
            arguments.put("x-dead-letter-routing-key", DEAD_ROUTING_KEY);

            // 声明正常队列及交换机
            channel.queueDeclare(QUEUE_NAME, true, false, false, arguments);
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
