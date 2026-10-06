package com.nicolasgabriel.lebuzzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;

class BuzzerServiceTest {

    private BuzzerService buzzerService;

    @BeforeEach
    void setUp() {
        buzzerService = new BuzzerService();
    }

    @Test
    void shouldCreateGameWithValidCodeAndState() {
        Game game = buzzerService.createGame();

        assertNotNull(game);
        assertNotNull(game.getGameCode());
        assertEquals(5, game.getGameCode().length());
        assertEquals(GameStates.WAITING, game.getCurrentState());
        assertNotNull(game.getHostToken());
    }
}