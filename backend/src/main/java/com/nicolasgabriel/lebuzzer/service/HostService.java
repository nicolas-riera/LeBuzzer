package com.nicolasgabriel.lebuzzer.service;

import static com.nicolasgabriel.lebuzzer.service.GameUtils.requireHost;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import com.nicolasgabriel.lebuzzer.model.Game;

class HostService {
    private final GameRegistry registry;
    private final GameSchedulerService scheduler;
    private final Duration hostGracePeriod;
    private final Consumer<String> onGameClosed;

    HostService(GameRegistry registry, GameSchedulerService scheduler, Duration hostGracePeriod,
            Consumer<String> onGameClosed) {
        this.registry = registry;
        this.scheduler = scheduler;
        this.hostGracePeriod = hostGracePeriod;
        this.onGameClosed = onGameClosed;
    }

    Game createGame() {
        Game game = registry.create();
        scheduleClosing(game.getGameCode());
        return game;
    }

    void connectHost(String gameCode, String hostToken, String sessionId) {
        Game game = registry.require(gameCode);
        synchronized (game) {
            requireHost(game, hostToken);
            String previousSessionId = game.getHostSessionId();
            if (previousSessionId != null) {
                registry.unbindHostSession(previousSessionId);
            }
            game.setHostSessionId(sessionId);
            registry.bindHostSession(sessionId, game.getGameCode());
            scheduler.cancelClosing(game.getGameCode());
        }
    }

    Optional<String> disconnectHost(String gameCode, String sessionId) {
        Optional<Game> found = registry.find(gameCode);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Game game = found.get();
        synchronized (game) {
            if (sessionId.equals(game.getHostSessionId())) {
                game.setHostSessionId(null);
                scheduleClosing(gameCode);
            }
        }
        return Optional.of(gameCode);
    }

    private void scheduleClosing(String gameCode) {
        scheduler.scheduleClosing(gameCode, hostGracePeriod.toMillis(), () -> closeAbandonedGame(gameCode));
    }

    private void closeAbandonedGame(String gameCode) {
        Optional<Game> found = registry.find(gameCode);
        if (found.isEmpty()) {
            return;
        }
        Game game = found.get();
        synchronized (game) {
            if (game.getHostSessionId() != null) {
                return;
            }
            registry.remove(game);
            scheduler.cancelAll(gameCode);
        }
        onGameClosed.accept(gameCode);
    }
}
