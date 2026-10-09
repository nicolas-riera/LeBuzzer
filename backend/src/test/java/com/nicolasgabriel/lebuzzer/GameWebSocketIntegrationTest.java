package com.nicolasgabriel.lebuzzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
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
import com.nicolasgabriel.lebuzzer.dto.SubmittedAnswer;
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
        buzzerService.connectHost(gameCode, game.getHostToken(), "host-session");

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

        Map<String, String> payload = Map.of("nickname", "Alice");
        session.send("/app/game/" + gameCode + "/join", payload);

        GameSnapshot snapshot = completableFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(snapshot);
    }

    @Test
    void testReloadedPlayerGetsBackFirstAnswer() throws Exception {
        Game game = buzzerService.createGame();
        String gameCode = game.getGameCode();
        buzzerService.connectHost(gameCode, game.getHostToken(), "host-session");
        buzzerService.startNextQuestion(gameCode, game.getHostToken());
        List<Integer> correct = game.getQuestionList().get(0).getCorrectAnswerIndices();
        List<Integer> wrong = List.of(correct.contains(0) ? 1 : 0);
        Map<String, String> join = Map.of("nickname", "Alice", "token", "alice-token");

        StompSession first = connect();
        CompletableFuture<SubmittedAnswer> firstAnswer = receiveAnswer(first);
        first.send("/app/game/" + gameCode + "/join", join);
        first.send("/app/game/" + gameCode + "/submit-answer", Map.of("selectedIndices", wrong));
        assertEquals(wrong, firstAnswer.get(5, TimeUnit.SECONDS).selectedIndices());
        first.disconnect();

        StompSession reloaded = connect();
        CompletableFuture<SubmittedAnswer> restored = receiveAnswer(reloaded);
        reloaded.send("/app/game/" + gameCode + "/join", join);
        assertEquals(wrong, restored.get(5, TimeUnit.SECONDS).selectedIndices());

        CompletableFuture<SubmittedAnswer> afterRetry = receiveAnswer(reloaded);
        reloaded.send("/app/game/" + gameCode + "/submit-answer", Map.of("selectedIndices", correct));
        assertEquals(wrong, afterRetry.get(5, TimeUnit.SECONDS).selectedIndices());
        assertEquals(1, buzzerService.getSnapshot(gameCode).answeredCount());
    }

    private StompSession connect() throws Exception {
        return stompClient.connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {})
                .get(3, TimeUnit.SECONDS);
    }

    private CompletableFuture<SubmittedAnswer> receiveAnswer(StompSession session) {
        CompletableFuture<SubmittedAnswer> future = new CompletableFuture<>();
        StompSession.Subscription subscription = session.subscribe("/user/queue/answer", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return SubmittedAnswer.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete((SubmittedAnswer) payload);
            }
        });
        return future.whenComplete((answer, error) -> subscription.unsubscribe());
    }
}
