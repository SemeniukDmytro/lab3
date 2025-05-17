package com.semeniuk.lab3.binance;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AuthenticatedWebSocketHandler webSocketHandler;

    public WebSocketConfig(AuthenticatedWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/btc")
                .setAllowedOrigins("*");
    }
}
