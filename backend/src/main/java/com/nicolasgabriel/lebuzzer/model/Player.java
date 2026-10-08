package com.nicolasgabriel.lebuzzer.model;

import com.nicolasgabriel.lebuzzer.enums.PlayerStates;

public class Player {
    private String id;
    private String sessionId;
    private String token;
    private int score;
    private PlayerStates playerStatus;

    public Player(String id, String sessionId) {
        this(id, sessionId, null);
    }

    public Player(String id, String sessionId, String token) {
        this.id = id;
        this.sessionId = sessionId;
        this.token = token;
        this.score = 0;
        this.playerStatus = PlayerStates.ONLINE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public PlayerStates getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStates playerStatus) {
        this.playerStatus = playerStatus;
    }
}