package com.nicolasgabriel.lebuzzer.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.Question;

final class GameUtils {
    static final long ANSWER_GRACE_MILLIS = 500;

    private GameUtils() {
    }

    static Question currentQuestion(Game game) {
        return game.getQuestionList().get(game.getCurrentQuestionIndex());
    }

    static long remainingMillis(Game game, Question question) {
        long elapsed = Duration.between(game.getQuestionStartTime(), LocalDateTime.now()).toMillis();
        return question.getDurationInSeconds() * 1000L - elapsed;
    }

    static Optional<Player> findPlayerBySession(Game game, String sessionId) {
        return game.getPlayerList().stream().filter(p -> p.getSessionId().equals(sessionId)).findFirst();
    }

    static Optional<Player> findPlayerByNickname(Game game, String nickname) {
        return game.getPlayerList().stream().filter(p -> p.getId().equalsIgnoreCase(nickname)).findFirst();
    }

    static void requireHost(Game game, String hostToken) {
        if (hostToken == null || !hostToken.equals(game.getHostToken())) {
            throw new SecurityException("Only the host can do this");
        }
    }
}
