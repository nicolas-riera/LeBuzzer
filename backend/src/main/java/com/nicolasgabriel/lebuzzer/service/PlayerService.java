package com.nicolasgabriel.lebuzzer.service;

import static com.nicolasgabriel.lebuzzer.service.GameUtils.ANSWER_GRACE_MILLIS;
import static com.nicolasgabriel.lebuzzer.service.GameUtils.currentQuestion;
import static com.nicolasgabriel.lebuzzer.service.GameUtils.findPlayerByNickname;
import static com.nicolasgabriel.lebuzzer.service.GameUtils.findPlayerBySession;
import static com.nicolasgabriel.lebuzzer.service.GameUtils.remainingMillis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.nicolasgabriel.lebuzzer.dto.SubmittedAnswer;
import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.enums.PlayerStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.PlayerAnswer;
import com.nicolasgabriel.lebuzzer.model.Question;

class PlayerService {
    private final GameRegistry registry;

    PlayerService(GameRegistry registry) {
        this.registry = registry;
    }

    Player joinGame(String gameCode, String nickname, String token, String sessionId) {
        Game game = registry.require(gameCode);
        String cleanNickname = nickname == null ? "" : nickname.trim();
        if (cleanNickname.isEmpty() || cleanNickname.length() > 20) {
            throw new IllegalArgumentException("Nickname must be between 1 and 20 characters");
        }
        synchronized (game) {
            if (game.getCurrentState() == GameStates.FINISHED) {
                throw new IllegalStateException("Game is finished");
            }
            Optional<Player> existing = findPlayerByNickname(game, cleanNickname);
            boolean resuming = existing.isPresent() && token != null && token.equals(existing.get().getToken());
            if (!resuming && game.getHostSessionId() == null) {
                throw new IllegalStateException("The host is not connected");
            }
            Player player;
            if (existing.isPresent()) {
                player = existing.get();
                if (!resuming && (player.getPlayerStatus() == PlayerStates.ONLINE || player.getToken() != null)) {
                    throw new IllegalStateException("Nickname already taken");
                }
                registry.unbindPlayerSession(player.getSessionId());
                player.setSessionId(sessionId);
                player.setPlayerStatus(PlayerStates.ONLINE);
            } else {
                player = new Player(cleanNickname, sessionId, token);
                game.getPlayerList().add(player);
            }
            registry.bindPlayerSession(sessionId, game.getGameCode());
            return player;
        }
    }

    boolean submitAnswer(String gameCode, String sessionId, List<Integer> selectedIndices) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            Player player = findPlayerBySession(game, sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown player"));
            if (game.getCurrentState() != GameStates.QUIZZING) {
                return false;
            }
            Question question = currentQuestion(game);
            if (remainingMillis(game, question) + ANSWER_GRACE_MILLIS <= 0) {
                return false;
            }
            if (game.getCurrentAnswers().containsKey(player.getId())) {
                return false;
            }
            List<Integer> indices = selectedIndices == null ? List.of() : selectedIndices;
            for (Integer index : indices) {
                if (index == null || index < 0 || index >= question.getOptions().size()) {
                    throw new IllegalArgumentException("Invalid option index");
                }
            }
            game.getCurrentAnswers().put(player.getId(),
                    new PlayerAnswer(player.getId(), question.getId(), new ArrayList<>(new HashSet<>(indices))));
            return true;
        }
    }

    Optional<SubmittedAnswer> getPlayerAnswer(String gameCode, String sessionId) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            GameStates state = game.getCurrentState();
            if (state != GameStates.QUIZZING && state != GameStates.QUIZ_RESULTS
                    && state != GameStates.LEADERBOARD) {
                return Optional.empty();
            }
            return findPlayerBySession(game, sessionId).flatMap(player -> {
                PlayerAnswer answer = game.getCurrentAnswers().get(player.getId());
                if (answer == null) {
                    return Optional.empty();
                }
                int scoreBefore = state == GameStates.QUIZZING
                        ? player.getScore()
                        : player.getScore() - answer.getPoints();
                return Optional.of(new SubmittedAnswer(
                        game.getCurrentQuestionIndex() + 1,
                        answer.getSelectedOptionIndices(),
                        scoreBefore));
            });
        }
    }

    Optional<String> disconnect(String sessionId) {
        String gameCode = registry.unbindPlayerSession(sessionId);
        if (gameCode == null) {
            return Optional.empty();
        }
        Optional<Game> found = registry.find(gameCode);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Game game = found.get();
        synchronized (game) {
            findPlayerBySession(game, sessionId).ifPresent(p -> p.setPlayerStatus(PlayerStates.OFFLINE));
        }
        return Optional.of(gameCode);
    }
}
