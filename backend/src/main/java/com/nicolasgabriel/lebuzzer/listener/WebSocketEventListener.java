package com.nicolasgabriel.lebuzzer.listener;

import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@Component
public class WebSocketEventListener {

    private final BuzzerService buzzerService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(BuzzerService buzzerService, SimpMessagingTemplate messagingTemplate) {
        this.buzzerService = buzzerService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Optional<String> gameCodeOpt = buzzerService.disconnect(sessionId);

        gameCodeOpt.ifPresent(gameCode -> {
            GameSnapshot snapshot = buzzerService.getSnapshot(gameCode);
            messagingTemplate.convertAndSend("/topic/game/" + gameCode, snapshot);
        });
    }
}