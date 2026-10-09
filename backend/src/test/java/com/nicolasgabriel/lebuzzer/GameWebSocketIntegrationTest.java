package com.nicolasgabriel.lebuzzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BuzzerService buzzerService;

    private WebSocketStompClient stompClient;

    @SuppressWarnings("removal")
    @BeforeEach
    void setup() {
        this.stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        this.stompClient.setMessageConverter(new CompositeMessageConverter(
                List.of(new MappingJackson2MessageConverter())
        ));
    }

    @Test
    void testJoinGameStomp() throws Exception {
        Game game = buzzerService.createGame();
        String gameCode = game.getGameCode();

        String url = "ws://localhost:" + port + "/ws";
        StompSession session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get(3, TimeUnit.SECONDS);

        CompletableFuture<GameSnapshot> completableFuture = new CompletableFuture<>();

        session.subscribe("/topic/game/" + gameCode, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameSnapshot.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete((GameSnapshot) payload);
            }
        });

        session.send("/app/game/" + gameCode + "/join", "Joueur1");

        GameSnapshot snapshot = completableFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(snapshot);
    }
}