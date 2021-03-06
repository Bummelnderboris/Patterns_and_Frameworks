package de.ostfalia.snakecore.SnakeServer.ws.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * @author Benjamin Wulfert
 *
 * The WebSocketConfig is a spring related class which configures the aspects of the WebSocket/STOMP-Protocol related software components.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /*
            /snakeserver is the HTTP URL for the endpoint to which a WebSocket (or SockJS)
            client needs to connect for the WebSocket handshake.
         */
        registry.addEndpoint("/snakeserver").withSockJS();
        registry.addEndpoint("/snakeserver");
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        /*
         	STOMP messages whose destination header begins with /app are routed to
            @MessageMapping methods in @Controller classes.
        */
        config.setApplicationDestinationPrefixes("/app");


        /*
            Use the built-in message broker for subscriptions and broadcasting and
            route messages whose destination header begins with /topic `or `/queue to the broker.
        */
        config.enableSimpleBroker("/topic");
    }

}