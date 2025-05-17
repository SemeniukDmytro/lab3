package com.semeniuk.lab3.binance;

import com.semeniuk.lab3.jwt.JwtDecoder;
import com.semeniuk.lab3.openidconnect.OpenIdConnectProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@Component
public class AuthenticatedWebSocketHandler extends TextWebSocketHandler {

    private final JwtDecoder jwtDecoder;
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    public AuthenticatedWebSocketHandler(OpenIdConnectProperties props) {
        this.jwtDecoder = new JwtDecoder(props);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromCookies(session);
        if (token == null || !isValidToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
            return;
        }

        sessions.add(session);
        System.out.println("✅ WebSocket client connected: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    private boolean isValidToken(String token) {
        try {
            jwtDecoder.decodeToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getTokenFromCookies(WebSocketSession session) {
        List<String> cookieHeaders = session.getHandshakeHeaders().get("cookie");
        if (cookieHeaders != null) {
            for (String header : cookieHeaders) {
                for (String cookie : header.split(";")) {
                    String[] parts = cookie.trim().split("=");
                    if (parts.length == 2 && parts[0].equals("access_token")) {
                        return parts[1];
                    }
                }
            }
        }
        return null;
    }

    public void broadcastProtobuf(byte[] protobufBytes) {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new BinaryMessage(protobufBytes));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
