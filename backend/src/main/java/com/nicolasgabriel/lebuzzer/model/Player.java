package com.nicolasgabriel.lebuzzer.model;

import com.nicolasgabriel.lebuzzer.enums.playerStates;

public class Player {
    private String id;
    private String sessionId;
    private int score;
    private playerStates playerStatus;

    public Player(String id, String sessionId) {
        this.id = id;
        this.sessionId = sessionId;
        this.score = 0;
        this.playerStatus = playerStates.ONLINE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public playerStates getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(playerStates playerStatus) {
        this.playerStatus = playerStatus;
    }
}