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

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
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
                () -> buzzerService.joinGame(game.getGameCode(), "Alice", "alice-token", "player-session"));
        assertFalse(buzzerService.getSnapshot(game.getGameCode()).hostConnected());
    }

    @Test
    void shouldAcceptJoinWhenHostIsConnected() {
        Game game = buzzerService.createGame();
        buzzerService.connectHost(game.getGameCode(), game.getHostToken(), "host-session");

        buzzerService.joinGame(game.getGameCode(), "Alice", "alice-token", "player-session");

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
        assertEquals(game.getQuestionList().get(0).getDurationInSeconds(), question.durationInSeconds());
        assertEquals(game.getQuestionList().get(0).getCorrectAnswerIndices().size() > 1, question.multipleChoice());
    }

    @Test
    void shouldKeepQuestionAndCorrectAnswersWhenLeaderboardIsShown() {
        Game game = buzzerService.createGame();
        buzzerService.connectHost(game.getGameCode(), game.getHostToken(), "host-session");
        buzzerService.startNextQuestion(game.getGameCode(), game.getHostToken());
        buzzerService.closeQuestion(game.getGameCode(), game.getHostToken());

        buzzerService.showLeaderboard(game.getGameCode(), game.getHostToken());

        GameSnapshot snapshot = buzzerService.getSnapshot(game.getGameCode());
        assertEquals(GameStates.LEADERBOARD, snapshot.state());
        assertNotNull(snapshot.currentQuestion());
        assertEquals(game.getQuestionList().get(0).getCorrectAnswerIndices(), snapshot.correctAnswerIndices());
    }

    @Test
    void shouldFinishGameWhileQuestionIsInProgress() {
        Game game = buzzerService.createGame();
        buzzerService.connectHost(game.getGameCode(), game.getHostToken(), "host-session");
        buzzerService.startNextQuestion(game.getGameCode(), game.getHostToken());

        buzzerService.finishGame(game.getGameCode(), game.getHostToken());

        assertEquals(GameStates.FINISHED, buzzerService.getSnapshot(game.getGameCode()).state());
    }

    @Test
    void shouldKeepOnlyTheFirstAnswerOfAPlayer() {
        Game game = startedGameWithPlayers("Alice");
        String code = game.getGameCode();
        List<Integer> correct = game.getQuestionList().get(0).getCorrectAnswerIndices();
        List<Integer> wrong = List.of(correct.contains(0) ? 1 : 0);

        assertTrue(buzzerService.submitAnswer(code, "Alice-session", wrong));
        assertFalse(buzzerService.submitAnswer(code, "Alice-session", correct));
        buzzerService.closeQuestion(code, game.getHostToken());

        assertEquals(1, buzzerService.getSnapshot(code).answeredCount());
        assertEquals(0, buzzerService.getSnapshot(code).leaderboard().get(0).score());
    }

    @Test
    void shouldRemovePlayerFromOnlineListWithoutStoppingTheGame() {
        Game game = startedGameWithPlayers("Alice", "Bob");
        String code = game.getGameCode();

        buzzerService.disconnect("Alice-session");

        GameSnapshot snapshot = buzzerService.getSnapshot(code);
        assertEquals(List.of("Bob"), snapshot.onlinePlayers());
        assertEquals(GameStates.QUIZZING, snapshot.state());
        assertTrue(buzzerService.submitAnswer(code, "Bob-session", List.of(0)));
    }

    @Test
    void shouldKeepTwoGamesIsolated() {
        Game first = startedGameWithPlayers("Alice");
        Game second = startedGameWithPlayers("Bob");

        assertThrows(IllegalArgumentException.class,
                () -> buzzerService.submitAnswer(second.getGameCode(), "Alice-session", List.of(0)));
        assertThrows(SecurityException.class,
                () -> buzzerService.closeQuestion(second.getGameCode(), first.getHostToken()));
        assertEquals(List.of("Alice"), buzzerService.getSnapshot(first.getGameCode()).onlinePlayers());
        assertEquals(List.of("Bob"), buzzerService.getSnapshot(second.getGameCode()).onlinePlayers());
        assertEquals(0, buzzerService.getSnapshot(second.getGameCode()).answeredCount());
    }

    @Test
    void shouldLetAPlayerJoinDuringAQuestionAndSeeTheCurrentState() {
        Game game = startedGameWithPlayers("Alice");
        String code = game.getGameCode();

        buzzerService.joinGame(code, "Late", "late-token", "late-session");

        GameSnapshot snapshot = buzzerService.getSnapshot(code);
        assertEquals(GameStates.QUIZZING, snapshot.state());
        assertNotNull(snapshot.currentQuestion());
        assertTrue(snapshot.remainingMillis() > 0);
        assertEquals(2, snapshot.leaderboard().size());
        assertTrue(buzzerService.submitAnswer(code, "late-session", List.of(0)));
    }

    @Test
    void shouldResumeWithTokenEvenIfOldConnectionIsStillOnline() {
        Game game = startedGameWithPlayers("Alice");
        String code = game.getGameCode();

        buzzerService.joinGame(code, "Alice", "Alice-token", "Alice-new-session");

        assertEquals(List.of("Alice"), buzzerService.getSnapshot(code).onlinePlayers());
        assertTrue(buzzerService.submitAnswer(code, "Alice-new-session", List.of(0)));
        assertTrue(buzzerService.disconnect("Alice-session").isEmpty());
    }

    @Test
    void shouldRefuseNicknameWithoutTheRightToken() {
        Game game = startedGameWithPlayers("Alice");
        String code = game.getGameCode();

        assertThrows(IllegalStateException.class,
                () -> buzzerService.joinGame(code, "alice", "other-token", "intruder-session"));
        buzzerService.disconnect("Alice-session");
        assertThrows(IllegalStateException.class,
                () -> buzzerService.joinGame(code, "Alice", "other-token", "intruder-session"));
    }

    @Test
    void shouldIncreaseSnapshotSequence() {
        Game game = buzzerService.createGame();

        long first = buzzerService.getSnapshot(game.getGameCode()).sequence();
        long second = buzzerService.getSnapshot(game.getGameCode()).sequence();

        assertTrue(second > first);
    }

    private Game startedGameWithPlayers(String... nicknames) {
        Game game = buzzerService.createGame();
        buzzerService.connectHost(game.getGameCode(), game.getHostToken(), game.getGameCode() + "-host");
        for (String nickname : nicknames) {
            buzzerService.joinGame(game.getGameCode(), nickname, nickname + "-token", nickname + "-session");
        }
        buzzerService.startNextQuestion(game.getGameCode(), game.getHostToken());
        return game;
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
        service.joinGame(game.getGameCode(), "Alice", "alice-token", "player-session");

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
