package com.nicolasgabriel.lebuzzer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@RestController 
@RequestMapping("/api/game")
public class GameRestController {

    private final BuzzerService service;
    
    public GameRestController(BuzzerService service) {
        this.service = service;
    }
}
