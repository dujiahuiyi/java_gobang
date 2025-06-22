package com.dujia.java_gobang.config;

import com.dujia.java_gobang.api.GameAPI;
import com.dujia.java_gobang.api.MatchAPI;
import com.dujia.java_gobang.api.TestAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private TestAPI testAPI;
    @Autowired
    private MatchAPI matchAPI;
    @Autowired
    private GameAPI gameAPI;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(testAPI, "/test");
        registry.addHandler(matchAPI, "/findMatch").addInterceptors(new HttpSessionHandshakeInterceptor());
        registry.addHandler(gameAPI, "/game").addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
