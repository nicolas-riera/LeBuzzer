package com.nicolasgabriel.lebuzzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlayerAnswer {
    private String playerId;
    private int questionId;
    private List<Integer> selectedOptionIndices;
    private LocalDateTime timestamp;
    private int points;

    public PlayerAnswer(String playerId, int questionId, List<Integer> selectedOptionIndices) {
        this.playerId = playerId;
        this.questionId = questionId;
        this.selectedOptionIndices = selectedOptionIndices != null ? selectedOptionIndices : new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public List<Integer> getSelectedOptionIndices() {
        return selectedOptionIndices;
    }

    public void setSelectedOptionIndices(List<Integer> selectedOptionIndices) {
        this.selectedOptionIndices = selectedOptionIndices;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}