package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.dto.AddUserDTO;
import com.czellmer1324.licenseplategame.dto.LoginDTO;
import com.czellmer1324.licenseplategame.dto.SpotStateDTO;
import com.czellmer1324.licenseplategame.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> addUser(@RequestBody AddUserDTO userInfo) {
        return service.addUser(userInfo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO info) {
        return service.login(info);
    }

    @GetMapping()
    public ResponseEntity<?> getUserInfo() {
        return service.getUserInfo();
    }

    @PostMapping("/mark-state")
    public ResponseEntity<?> markState(@RequestBody SpotStateDTO info) {
        return service.markState(info);
    }

    @DeleteMapping("/unmark-state/{markedStateId}")
    public ResponseEntity<?> deleteStateMark(@PathVariable Long markedStateId) {
        return service.unmarkState(markedStateId);
    }

    @GetMapping("/marked")
    public ResponseEntity<?> getMarkedStates() {
        return service.getMarkedStates();
    }
}
