package com.example.IndiChessBackend.config;

import com.example.IndiChessBackend.service.JwtService;
import com.example.IndiChessBackend.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final MyUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = null;

            // 🔐 Extract JWT from cookies
            if (accessor.getNativeHeader("cookie") != null) {
                for (String cookieHeader : accessor.getNativeHeader("cookie")) {
                    for (String cookie : cookieHeader.split(";")) {
                        cookie = cookie.trim();
                        if (cookie.startsWith("JWT=")) {
                            token = cookie.substring(4);
                        }
                    }
                }
            }

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);
                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(token, userDetails)) {

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        accessor.setUser(auth); // ✅ THIS IS CRITICAL
                        SecurityContextHolder.getContext()
                                .setAuthentication(auth);
                    }
                } catch (Exception e) {
                    System.out.println("Invalid JWT in WebSocket");
                }
            } else {
                System.out.println("No JWT token found in WebSocket connection");
            }
        }

        return message;
    }
}
