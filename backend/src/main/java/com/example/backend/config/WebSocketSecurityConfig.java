package com.example.backend.config;

import com.example.backend.security.JwtTokenProvider;
import com.example.backend.security.UserPrinciple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if(StompCommand.CONNECT.equals(accessor.getCommand())){
                    handleConnect(accessor);
                }
                return ChannelInterceptor.super.preSend(message, channel);
            }
        });
    }

    private void handleConnect(StompHeaderAccessor accessor){
        try{
            String authToken = accessor.getFirstNativeHeader("Authorization");
            if(authToken != null && authToken.startsWith("Bearer")){
                String accessToken = authToken.substring(7);
                String email = jwtTokenProvider.extractSubject(accessToken);

                if(jwtTokenProvider.validateAccessToken(accessToken, userDetailsService.loadUserByUsername(jwtTokenProvider.extractSubject(accessToken)))){
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(accessToken, null, userDetailsService.loadUserByUsername(jwtTokenProvider.extractSubject(accessToken)).getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(token);
                    accessor.setUser(token);
                }

                log.info("Connected to websocket!: " + email);
            }else{
                log.error("Invalid token!");
            }
        }catch (Exception e){
            log.error("Error while connecting to websocket: " + e.getMessage());
        }
    }
}
