package com.unifor.br.chat_peer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // É aqui que o nosso HTML/Javascript vai se conectar inicialmente
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/topic" é o canal por onde o servidor envia coisas para o navegador
        registry.enableSimpleBroker("/topic");
        // "/app" é o prefixo para quando o navegador quiser enviar algo para o servidor
        registry.setApplicationDestinationPrefixes("/app");
    }
}