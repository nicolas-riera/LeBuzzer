package com.nicolasgabriel.lebuzzer.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.model.Player;
import com.nicolasgabriel.lebuzzer.model.Question;
import com.nicolasgabriel.lebuzzer.model.QuestionCatalog;

class GameRegistry {
    private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 5;
    private static final int QUESTIONS_PER_GAME = 10;

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, String> gameCodeBySessionId = new ConcurrentHashMap<>();
    private final Map<String, String> hostedGameCodeBySessionId = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    Game create() {
        List<Question> questions = new ArrayList<>(QuestionCatalog.getAllQuestions());
        Collections.shuffle(questions, random);
        questions = new ArrayList<>(questions.subList(0, Math.min(QUESTIONS_PER_GAME, questions.size())));

        Game game;
        do {
            game = new Game(generateCode(), questions);
        } while (games.putIfAbsent(game.getGameCode(), game) != null);
        game.setHostToken(UUID.randomUUID().toString());
        return game;
    }

    Game require(String gameCode) {
        Game game = gameCode == null ? null : games.get(gameCode.toUpperCase());
        if (game == null) {
            throw new IllegalArgumentException("Unknown game code");
        }
        return game;
    }

    Optional<Game> find(String gameCode) {
        return Optional.ofNullable(gameCode).map(games::get);
    }

    void remove(Game game) {
        games.remove(game.getGameCode());
        for (Player player : game.getPlayerList()) {
            gameCodeBySessionId.remove(player.getSessionId());
        }
    }

    void bindPlayerSession(String sessionId, String gameCode) {
        gameCodeBySessionId.put(sessionId, gameCode);
    }

    String unbindPlayerSession(String sessionId) {
        return gameCodeBySessionId.remove(sessionId);
    }

    void bindHostSession(String sessionId, String gameCode) {
        hostedGameCodeBySessionId.put(sessionId, gameCode);
    }

    String unbindHostSession(String sessionId) {
        return hostedGameCodeBySessionId.remove(sessionId);
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return code.toString();
    }
}
