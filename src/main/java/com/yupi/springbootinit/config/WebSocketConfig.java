package com.yupi.springbootinit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类。开启WebSocket的支持
 */
@Configuration
public class WebSocketConfig {

    /**
     * bean注册：会自动扫描带有@ServerEndpoint注解声明的Websocket Endpoint(端点)，注册成为Websocket bean。
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
