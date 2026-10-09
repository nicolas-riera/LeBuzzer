package com.nicolasgabriel.lebuzzer;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nicolasgabriel.lebuzzer.controller.GameRestController;
import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.enums.GameStates;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@WebMvcTest(GameRestController.class)
class GameRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuzzerService buzzerService;

    @Test
    void shouldCreateGame() throws Exception {
        Game mockGame = new Game("ABCDE", List.of());
        given(buzzerService.createGame()).willReturn(mockGame);

        mockMvc.perform(post("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("ABCDE"));
    }

    @Test
    void shouldGetSnapshot() throws Exception {
        GameSnapshot mockSnapshot = new GameSnapshot(
                "ABCDE", GameStates.WAITING, List.of(), null, 0, 0, null, List.of(), true, 1
        );
        given(buzzerService.getSnapshot("ABCDE")).willReturn(mockSnapshot);

        mockMvc.perform(get("/api/games/ABCDE/snapshot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("ABCDE"))
                .andExpect(jsonPath("$.state").value("WAITING"));
    }

    @Test
    void shouldReturnNotFoundForUnknownGame() throws Exception {
        given(buzzerService.getSnapshot("ZZZZZ")).willThrow(new IllegalArgumentException("Unknown game code"));

        mockMvc.perform(get("/api/games/ZZZZZ/snapshot"))
                .andExpect(status().isNotFound());
    }
}