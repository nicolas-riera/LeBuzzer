package com.nicolasgabriel.lebuzzer.dto;

import java.util.List;

public record SubmittedAnswer(int questionNumber, List<Integer> selectedIndices, int scoreBefore) {
}
