package com.nicolasgabriel.lebuzzer.dto;

import java.util.List;

public record QuestionView(
        int id,
        int number,
        int totalQuestions,
        String text,
        List<String> options,
        boolean multipleChoice,
        int durationInSeconds) {
}
