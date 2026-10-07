package com.nicolasgabriel.lebuzzer.controller;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@Controller
public class GameWebSocketController {

    private final BuzzerService buzzerService;

    public GameWebSocketController(BuzzerService buzzerService) {
        this.buzzerService = buzzerService;
    }

    @MessageMapping("/game/{gameCode}/join")
    public void joinGame(@DestinationVariable String gameCode, String nickname, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        buzzerService.joinGame(gameCode, nickname, sessionId);
    }

    @MessageMapping("/game/{gameCode}/answer")
    public void submitAnswer(@DestinationVariable String gameCode, List<Integer> selectedIndices, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        buzzerService.submitAnswer(gameCode, sessionId, selectedIndices);
    }

    @MessageMapping("/game/{gameCode}/start")
    public void startNextQuestion(@DestinationVariable String gameCode, @Header("hostToken") String hostToken) {
        buzzerService.startNextQuestion(gameCode, hostToken);
    }

    @MessageMapping("/game/{gameCode}/close")
    public void closeQuestion(@DestinationVariable String gameCode, @Header("hostToken") String hostToken) {
        buzzerService.closeQuestion(gameCode, hostToken);
    }
}