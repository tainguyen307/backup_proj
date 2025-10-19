// com.womtech.config.WebSocketConfig
package com.womtech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// Endpoint dành cho USER
		registry.addEndpoint("/user/chat-ws").setAllowedOriginPatterns("*").withSockJS();

		// Endpoint dành cho VENDOR
		registry.addEndpoint("/vendor/chat-ws").setAllowedOriginPatterns("*").withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app"); // client send
		registry.enableSimpleBroker("/topic", "/queue"); // server push
		// Có thể thêm user destination nếu cần:
		// registry.setUserDestinationPrefix("/user");
	}
}
