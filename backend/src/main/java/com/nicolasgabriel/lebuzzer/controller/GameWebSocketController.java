package com.nicolasgabriel.lebuzzer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nicolasgabriel.lebuzzer.service.BuzzerService;

@Controller 
@RequestMapping("/game")
public class GameWebSocketController {
    
    private final BuzzerService service;

    public GameWebSocketController(BuzzerService service) {
        this.service = service;
    }
}
