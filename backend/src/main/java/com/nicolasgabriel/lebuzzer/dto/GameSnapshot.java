package com.nicolasgabriel.lebuzzer.dto;

import java.util.List;

import com.nicolasgabriel.lebuzzer.enums.GameStates;


public record GameSnapshot(
        String gameCode,
        GameStates state,
        List<String> onlinePlayers,
        QuestionView currentQuestion,
        long remainingMillis,
        int answeredCount,
        List<Integer> correctAnswerIndices,
        List<LeaderboardEntry> leaderboard) {
}
