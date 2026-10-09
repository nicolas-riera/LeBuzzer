package com.nicolasgabriel.lebuzzer.service;

import static com.nicolasgabriel.lebuzzer.service.GameUtils.currentQuestion;
import static com.nicolasgabriel.lebuzzer.service.GameUtils.remainingMillis;

import java.util.List;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.dto.QuestionView;
import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.enums.PlayerStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.Question;

class GameViewFactory {
    private final ScoringService scoring;

    GameViewFactory(ScoringService scoring) {
        this.scoring = scoring;
    }

    GameSnapshot snapshot(Game game) {
        GameStates state = game.getCurrentState();
        boolean resultsVisible = state == GameStates.QUIZ_RESULTS || state == GameStates.LEADERBOARD;
        boolean questionVisible = state == GameStates.QUIZZING || resultsVisible;
        Question question = questionVisible ? currentQuestion(game) : null;
        QuestionView view = question == null ? null : questionView(game, question);
        long remaining = state == GameStates.QUIZZING ? Math.max(0, remainingMillis(game, question)) : 0;
        List<Integer> correct = resultsVisible ? question.getCorrectAnswerIndices() : null;
        return new GameSnapshot(
                game.getGameCode(),
                state,
                onlineNicknames(game),
                view,
                remaining,
                game.getCurrentAnswers().size(),
                correct,
                scoring.buildLeaderboard(game),
                game.getHostSessionId() != null,
                game.nextSnapshotSequence());
    }

    QuestionView questionView(Game game, Question question) {
        return new QuestionView(
                question.getId(),
                game.getCurrentQuestionIndex() + 1,
                game.getQuestionList().size(),
                question.getText(),
                List.copyOf(question.getOptions()),
                question.getCorrectAnswerIndices().size() > 1,
                question.getDurationInSeconds());
    }

    private List<String> onlineNicknames(Game game) {
        return game.getPlayerList().stream()
                .filter(p -> p.getPlayerStatus() == PlayerStates.ONLINE)
                .map(Player::getId)
                .toList();
    }
}
