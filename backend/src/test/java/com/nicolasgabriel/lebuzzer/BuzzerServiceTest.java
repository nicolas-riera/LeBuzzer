package com.nicolasgabriel.lebuzzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nicolasgabriel.lebuzzer.dto.QuestionView;
import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;

class BuzzerServiceTest {

    private static final Duration SHORT_GRACE_PERIOD = Duration.ofMillis(100);

    private BuzzerService buzzerService;

    @BeforeEach
    void setUp() {
        buzzerService = new BuzzerService();
    }

    @Test
    void shouldCreateGameWithValidCodeAndState() {
        Game game = buzzerService.createGame();

        assertNotNull(game);
        assertNotNull(game.getGameCode());
        assertEquals(5, game.getGameCode().length());
        assertEquals(GameStates.WAITING, game.getCurrentState());
        assertNotNull(game.getHostToken());
    }

    @Test
    void shouldRefuseJoinWhenHostIsNotConnected() {
        Game game = buzzerService.createGame();

        assertThrows(IllegalStateException.class,
                () -> buzzerService.joinGame(game.getGameCode(), "Alice", "player-session"));
        assertFalse(buzzerService.getSnapshot(game.getGameCode()).hostConnected());
    }

    @Test
    void shouldAcceptJoinWhenHostIsConnected() {
        Game game = buzzerService.createGame();
        buzzerService.connectHost(game.getGameCode(), game.getHostToken(), "host-session");

        buzzerService.joinGame(game.getGameCode(), "Alice", "player-session");

        assertTrue(buzzerService.getSnapshot(game.getGameCode()).hostConnected());
        assertEquals(List.of("Alice"), buzzerService.getSnapshot(game.getGameCode()).onlinePlayers());
    }

    @Test
    void shouldExposeQuestionNumberAndTypeWhenQuestionStarts() {
        Game game = buzzerService.createGame();
        buzzerService.connectHost(game.getGameCode(), game.getHostToken(), "host-session");

        buzzerService.startNextQuestion(game.getGameCode(), game.getHostToken());

        QuestionView question = buzzerService.getSnapshot(game.getGameCode()).currentQuestion();
        assertEquals(1, question.number());
        assertEquals(game.getQuestionList().size(), question.totalQuestions());
        assertEquals(game.getQuestionList().get(0).getCorrectAnswerIndices().size() > 1, question.multipleChoice());
    }

    @Test
    void shouldRefuseHostConnectionWithWrongToken() {
        Game game = buzzerService.createGame();

        assertThrows(SecurityException.class,
                () -> buzzerService.connectHost(game.getGameCode(), "wrong-token", "host-session"));
    }

    @Test
    void shouldCloseGameWhenHostLeavesForGood() throws InterruptedException {
        BuzzerService service = new BuzzerService(SHORT_GRACE_PERIOD);
        CountDownLatch closed = new CountDownLatch(1);
        service.setGameClosedListener(code -> closed.countDown());
        Game game = service.createGame();
        service.connectHost(game.getGameCode(), game.getHostToken(), "host-session");
        service.joinGame(game.getGameCode(), "Alice", "player-session");

        service.disconnect("host-session");

        assertTrue(closed.await(2, TimeUnit.SECONDS));
        assertTrue(service.findGame(game.getGameCode()).isEmpty());
        assertTrue(service.disconnect("player-session").isEmpty());
    }

    @Test
    void shouldKeepGameWhenHostReconnectsInTime() throws InterruptedException {
        BuzzerService service = new BuzzerService(SHORT_GRACE_PERIOD);
        CountDownLatch closed = new CountDownLatch(1);
        service.setGameClosedListener(code -> closed.countDown());
        Game game = service.createGame();
        service.connectHost(game.getGameCode(), game.getHostToken(), "host-session");

        service.disconnect("host-session");
        service.connectHost(game.getGameCode(), game.getHostToken(), "new-host-session");

        assertFalse(closed.await(500, TimeUnit.MILLISECONDS));
        assertTrue(service.findGame(game.getGameCode()).isPresent());
    }

    @Test
    void shouldCloseGameWhenHostNeverConnects() throws InterruptedException {
        BuzzerService service = new BuzzerService(SHORT_GRACE_PERIOD);
        CountDownLatch closed = new CountDownLatch(1);
        service.setGameClosedListener(code -> closed.countDown());
        Game game = service.createGame();

        assertTrue(closed.await(2, TimeUnit.SECONDS));
        assertTrue(service.findGame(game.getGameCode()).isEmpty());
    }
}
