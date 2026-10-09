package com.nicolasgabriel.lebuzzer.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private int id;
    private String text;
    private List<String> options; 
    private List<Integer> correctAnswerIndices; 
    private int durationInSeconds;

    public Question(int id, String text, List<String> options, List<Integer> correctAnswerIndices, int durationInSeconds) {
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException("A question needs 4 answers.");
        }
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswerIndices = correctAnswerIndices != null ? correctAnswerIndices : new ArrayList<>();
        this.durationInSeconds = durationInSeconds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException("A question needs 4 answers.");
        }
        this.options = options;
    }

    public List<Integer> getCorrectAnswerIndices() {
        return correctAnswerIndices;
    }

    public void setCorrectAnswerIndices(List<Integer> correctAnswerIndices) {
        this.correctAnswerIndices = correctAnswerIndices;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }
}