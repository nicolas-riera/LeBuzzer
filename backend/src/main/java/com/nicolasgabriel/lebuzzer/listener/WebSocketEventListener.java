package com.nicolasgabriel.lebuzzer.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@Component
public class WebSocketEventListener {

    private final BuzzerService buzzerService;

    public WebSocketEventListener(BuzzerService buzzerService) {
        this.buzzerService = buzzerService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            buzzerService.disconnect(sessionId);
        }
    }
}