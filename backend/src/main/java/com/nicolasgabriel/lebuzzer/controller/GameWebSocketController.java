package com.nicolasgabriel.lebuzzer.controller;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.Question;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;

import jakarta.annotation.PostConstruct;

@Controller
public class GameWebSocketController {

    private final BuzzerService buzzerService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(BuzzerService buzzerService, SimpMessagingTemplate messagingTemplate) {
        this.buzzerService = buzzerService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    public void init() {
        buzzerService.setQuestionExpiredListener(gameCode -> {
            broadcastSnapshot(gameCode);
        });
    }

    @MessageMapping("/game/{gameCode}/join")
    public void joinGame(@DestinationVariable String gameCode,
                         @Payload Map<String, String> payload,
                         @Header("simpSessionId") String sessionId) {
        String nickname = payload.get("nickname");
        Player player = buzzerService.joinGame(gameCode, nickname, sessionId);
        
        messagingTemplate.convertAndSend("/topic/game/" + gameCode + "/player-joined", player);
        broadcastSnapshot(gameCode);
    }

    @MessageMapping("/game/{gameCode}/start-next-question")
    public void startNextQuestion(@DestinationVariable String gameCode,
                                  @Header("hostToken") String hostToken) {
        @SuppressWarnings("unused")
        Question question = buzzerService.startNextQuestion(gameCode, hostToken);
        broadcastSnapshot(gameCode);
    }

    @MessageMapping("/game/{gameCode}/submit-answer")
    public void submitAnswer(@DestinationVariable String gameCode,
                             @Payload Map<String, List<Integer>> payload,
                             @Header("simpSessionId") String sessionId) {
        List<Integer> selectedIndices = payload.get("selectedIndices");
        boolean accepted = buzzerService.submitAnswer(gameCode, sessionId, selectedIndices);
        
        if (accepted) {
            broadcastSnapshot(gameCode);
        }
    }

    @MessageMapping("/game/{gameCode}/close-question")
    public void closeQuestion(@DestinationVariable String gameCode,
                              @Header("hostToken") String hostToken) {
        buzzerService.closeQuestion(gameCode, hostToken);
        broadcastSnapshot(gameCode);
    }

    @MessageMapping("/game/{gameCode}/show-leaderboard")
    public void showLeaderboard(@DestinationVariable String gameCode,
                                @Header("hostToken") String hostToken) {
        buzzerService.showLeaderboard(gameCode, hostToken);
        broadcastSnapshot(gameCode);
    }

    @MessageMapping("/game/{gameCode}/finish")
    public void finishGame(@DestinationVariable String gameCode,
                           @Header("hostToken") String hostToken) {
        buzzerService.finishGame(gameCode, hostToken);
        broadcastSnapshot(gameCode);
    }

    private void broadcastSnapshot(String gameCode) {
        GameSnapshot snapshot = buzzerService.getSnapshot(gameCode);
        messagingTemplate.convertAndSend("/topic/game/" + gameCode, snapshot);
    }
}