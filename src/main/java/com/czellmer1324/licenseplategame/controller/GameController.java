package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.dto.SpotStateDTO;
import com.czellmer1324.licenseplategame.services.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameController {
    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping("/mark-state")
    public ResponseEntity<?> markState(@RequestBody SpotStateDTO info) {
        ServiceResponse response = service.markState(info);
        return ResponseEntity.status(response.code()).body(response.response());
    }

    @DeleteMapping("/unmark-state/{markedStateId}")
    public ResponseEntity<?> deleteStateMark(@PathVariable Long markedStateId) {
        ServiceResponse info = service.unmarkState(markedStateId);
        return ResponseEntity.status(info.code()).body(info.response());
    }

    @GetMapping("/marked")
    public ResponseEntity<?> getMarkedStates() {
        ServiceResponse info = service.getMarkedStates();
        return ResponseEntity.status(info.code()).body(info.response());
    }

    @DeleteMapping("/unmark-all")
    public ResponseEntity<?> unmarkAll() {
        ServiceResponse info = service.unmarkAll();
        return ResponseEntity.status(info.code()).body(info.response());
    }
}
