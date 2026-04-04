package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.mappings.requestobjects.AddUserDTO;
import com.czellmer1324.licenseplategame.mappings.requestobjects.LoginDTO;
import com.czellmer1324.licenseplategame.mappings.requestobjects.SpotStateDTO;
import com.czellmer1324.licenseplategame.mappings.returnobjects.*;
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
    public CreateUserResponse addUser(@RequestBody AddUserDTO userInfo) {
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

    // Updated this to new method
    @PostMapping("/mark-state")
    public ResponseEntity<?> markState(@RequestBody SpotStateDTO info) {
        return service.markState(info);
    }

    // Updated this to new method
    @DeleteMapping("/unmark-state/{markedStateId}")
    public ResponseEntity<?> deleteStateMark(@PathVariable Long markedStateId) {
        return service.unmarkState(markedStateId);
    }

    // Updated this to new way already
    @GetMapping("/marked")
    public ResponseEntity<?> getMarkedStates() {
        return service.getMarkedStates();
    }
}
