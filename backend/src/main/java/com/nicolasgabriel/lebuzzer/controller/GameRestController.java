package com.nicolasgabriel.lebuzzer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicolasgabriel.lebuzzer.dto.GameSnapshot;
import com.nicolasgabriel.lebuzzer.model.Game;
import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@RestController
@RequestMapping("/api/games")
public class GameRestController {

    private final BuzzerService buzzerService;

    public GameRestController(BuzzerService buzzerService) {
        this.buzzerService = buzzerService;
    }

    @PostMapping
    public ResponseEntity<Game> createGame() {
        Game newGame = buzzerService.createGame();
        return ResponseEntity.ok(newGame);
    }

    @GetMapping("/{gameCode}/snapshot")
    public ResponseEntity<GameSnapshot> getSnapshot(@PathVariable String gameCode) {
        GameSnapshot snapshot = buzzerService.getSnapshot(gameCode);
        return ResponseEntity.ok(snapshot);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleUnknownGame() {
        return ResponseEntity.notFound().build();
    }
}