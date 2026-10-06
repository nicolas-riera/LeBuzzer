package com.nicolasgabriel.lebuzzer.dto;

import java.util.List;

public record QuestionView(int id, String text, List<String> options, int durationInSeconds) {
}
