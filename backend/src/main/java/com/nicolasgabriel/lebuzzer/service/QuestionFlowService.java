package com.nicolasgabriel.lebuzzer.service;

import static com.nicolasgabriel.lebuzzer.service.GameUtils.ANSWER_GRACE_MILLIS;
import static com.nicolasgabriel.lebuzzer.service.GameUtils.currentQuestion;
import static com.nicolasgabriel.lebuzzer.service.GameUtils.requireHost;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Question;

class QuestionFlowService {
    private final GameRegistry registry;
    private final GameSchedulerService scheduler;
    private final ScoringService scoring;
    private final Consumer<String> onQuestionExpired;

    QuestionFlowService(GameRegistry registry, GameSchedulerService scheduler, ScoringService scoring,
            Consumer<String> onQuestionExpired) {
        this.registry = registry;
        this.scheduler = scheduler;
        this.scoring = scoring;
        this.onQuestionExpired = onQuestionExpired;
    }

    Question startNextQuestion(String gameCode, String hostToken) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            GameStates state = game.getCurrentState();
            if (state == GameStates.QUIZZING || state == GameStates.FINISHED) {
                throw new IllegalStateException("Cannot start a question while game is " + state);
            }
            int nextIndex = game.getCurrentQuestionIndex() + 1;
            if (nextIndex >= game.getQuestionList().size()) {
                throw new IllegalStateException("No more questions");
            }
            game.setCurrentQuestionIndex(nextIndex);
            game.getCurrentAnswers().clear();
            game.setQuestionStartTime(LocalDateTime.now());
            game.setCurrentState(GameStates.QUIZZING);

            Question question = game.getQuestionList().get(nextIndex);
            scheduleExpiration(game, question);
            return question;
        }
    }

    void closeQuestion(String gameCode, String hostToken) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            if (!closeQuestionLocked(game)) {
                throw new IllegalStateException("No question in progress");
            }
        }
    }

    void showLeaderboard(String gameCode, String hostToken) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            if (game.getCurrentState() != GameStates.QUIZ_RESULTS) {
                throw new IllegalStateException("Leaderboard is available after a question is closed");
            }
            game.setCurrentState(GameStates.LEADERBOARD);
        }
    }

    void finishGame(String gameCode, String hostToken) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            if (game.getCurrentState() == GameStates.QUIZZING) {
                closeQuestionLocked(game);
            }
            game.setCurrentState(GameStates.FINISHED);
        }
    }

    private void scheduleExpiration(Game game, Question question) {
        String code = game.getGameCode();
        int expectedIndex = game.getCurrentQuestionIndex();
        scheduler.scheduleExpiration(code, question.getDurationInSeconds() * 1000L + ANSWER_GRACE_MILLIS,
                () -> onExpiration(code, expectedIndex));
    }

    private void onExpiration(String gameCode, int questionIndex) {
        Optional<Game> found = registry.find(gameCode);
        if (found.isEmpty()) {
            return;
        }
        Game game = found.get();
        boolean closed;
        synchronized (game) {
            closed = game.getCurrentQuestionIndex() == questionIndex && closeQuestionLocked(game);
        }
        if (closed) {
            onQuestionExpired.accept(gameCode);
        }
    }

    private boolean closeQuestionLocked(Game game) {
        if (game.getCurrentState() != GameStates.QUIZZING) {
            return false;
        }
        scheduler.cancelExpiration(game.getGameCode());
        scoring.scoreAnswers(game, currentQuestion(game));
        game.setCurrentState(GameStates.QUIZ_RESULTS);
        return true;
    }
}
