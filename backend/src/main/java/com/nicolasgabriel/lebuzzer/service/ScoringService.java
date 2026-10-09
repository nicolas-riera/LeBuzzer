package com.nicolasgabriel.lebuzzer.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.nicolasgabriel.lebuzzer.dto.LeaderboardEntry;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.PlayerAnswer;
import com.nicolasgabriel.lebuzzer.model.Question;

class ScoringService {
    private static final int BASE_POINTS = 500;
    private static final int MAX_SPEED_BONUS = 500;

    void scoreAnswers(Game game, Question question) {
        for (Player player : game.getPlayerList()) {
            PlayerAnswer answer = game.getCurrentAnswers().get(player.getId());
            if (answer != null) {
                answer.setPoints(computePoints(game, question, answer));
                player.setScore(player.getScore() + answer.getPoints());
            }
        }
    }

    List<LeaderboardEntry> buildLeaderboard(Game game) {
        List<Player> sorted = game.getPlayerList().stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .toList();
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Player player = sorted.get(i);
            int rank = (i > 0 && sorted.get(i - 1).getScore() == player.getScore())
                    ? entries.get(i - 1).rank()
                    : i + 1;
            entries.add(new LeaderboardEntry(rank, player.getId(), player.getScore()));
        }
        return entries;
    }

    private int computePoints(Game game, Question question, PlayerAnswer answer) {
        if (!new HashSet<>(answer.getSelectedOptionIndices())
                .equals(new HashSet<>(question.getCorrectAnswerIndices()))) {
            return 0;
        }
        long durationMillis = question.getDurationInSeconds() * 1000L;
        long elapsed = Duration.between(game.getQuestionStartTime(), answer.getTimestamp()).toMillis();
        long remaining = Math.max(0, Math.min(durationMillis, durationMillis - elapsed));
        return BASE_POINTS + (int) (MAX_SPEED_BONUS * remaining / durationMillis);
    }
}
