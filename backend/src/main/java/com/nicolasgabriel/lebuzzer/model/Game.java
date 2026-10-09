package com.nicolasgabriel.lebuzzer.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nicolasgabriel.lebuzzer.enums.GameStates;

public class Game {
    private String gameCode;
    private String hostToken;
    private String hostSessionId;
    private GameStates currentState;
    private LocalDateTime questionStartTime;
    private int currentQuestionIndex;
    private List<Player> playerList;
    private List<Question> questionList;
    private Map<String, PlayerAnswer> currentAnswers;
    private long snapshotSequence;

    public Game(String gameCode) {
        this(gameCode, null);
    }

    public Game(String gameCode, List<Question> questionList) {
        this.gameCode = gameCode;
        this.currentState = GameStates.WAITING;
        this.currentQuestionIndex = -1;
        this.playerList = new ArrayList<>();
        this.questionList = questionList != null ? questionList : new ArrayList<>();
        this.currentAnswers = new HashMap<>();
    }

    public String getHostToken() {
        return hostToken;
    }

    public void setHostToken(String hostToken) {
        this.hostToken = hostToken;
    }

    public long nextSnapshotSequence() {
        return ++snapshotSequence;
    }

    public String getHostSessionId() {
        return hostSessionId;
    }

    public void setHostSessionId(String hostSessionId) {
        this.hostSessionId = hostSessionId;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public Map<String, PlayerAnswer> getCurrentAnswers() {
        return currentAnswers;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public GameStates getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameStates currentState) {
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