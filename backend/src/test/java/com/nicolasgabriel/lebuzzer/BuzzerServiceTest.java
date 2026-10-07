package com.nicolasgabriel.lebuzzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;
import com.nicolasgabriel.lebuzzer.service.GameSchedulerService;
import com.nicolasgabriel.lebuzzer.service.ScoringService;

class BuzzerServiceTest {

    private BuzzerService buzzerService;

    @BeforeEach
    void setUp() {
        ScoringService scoringService = new ScoringService();
        GameSchedulerService schedulerService = new GameSchedulerService();
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

        buzzerService = new BuzzerService(scoringService, schedulerService, messagingTemplate);
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