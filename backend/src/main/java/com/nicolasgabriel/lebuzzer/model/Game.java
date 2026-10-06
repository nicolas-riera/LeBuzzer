package com.nicolasgabriel.lebuzzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.nicolasgabriel.lebuzzer.enums.gameStates;

public class Game {
    private String gameCode;
    private gameStates currentState;
    private LocalDateTime questionStartTime;
    private List<Player> playerList;
    private List<Question> questionList;

    public Game(String gameCode) {
        this.gameCode = gameCode;
        this.currentState = gameStates.WAITING;
        this.playerList = new ArrayList<>();
        this.questionList = new ArrayList<>();
    }

    public Game(String gameCode, List<Question> questionList) {
        this.gameCode = gameCode;
        this.currentState = gameStates.WAITING;
        this.playerList = new ArrayList<>();
        this.questionList = questionList != null ? questionList : new ArrayList<>();
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public gameStates getCurrentState() {
        return currentState;
    }

    public void setCurrentState(gameStates currentState) {
        this.currentState = currentState;
    }

    public LocalDateTime getQuestionStartTime() {
        return questionStartTime;
    }

    public void setQuestionStartTime(LocalDateTime questionStartTime) {
        this.questionStartTime = questionStartTime;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }
}