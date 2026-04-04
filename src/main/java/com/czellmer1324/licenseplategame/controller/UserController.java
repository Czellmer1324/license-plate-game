package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.dto.*;
import com.czellmer1324.licenseplategame.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> addUser(@RequestBody AddUserDTO userInfo) {
        ServiceResponse info = service.addUser(userInfo);
        return ResponseEntity.status(info.code()).body(info.response());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO info) {
        ServiceResponse response = service.login(info);
        return ResponseEntity.status(response.code()).body(response.response());
    }

    @GetMapping()
    public ResponseEntity<?> getUserInfo() {
        ServiceResponse info = service.getUserInfo();
        return ResponseEntity.status(info.code()).body(info.response());
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
        Optional<Iterable<GetMarkedStatesDTO>> info = service.getMarkedStates();

        if (info.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(info.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Message", "User is not authenticated"));
        }
    }
}
