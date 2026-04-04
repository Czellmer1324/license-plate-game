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

    @PostMapping("login")
    public String login(@RequestBody LoginDTO info) {
        // will return JWT token, have separate method to retrieve user info after authenticated with JWT
        return service.login(info);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable int userId) {
        //This will change, and we will get the user ID from the JWT
        UserReturnInfo userInfo = service.getUserInfo(userId);
        if (userInfo.id() == -1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(userInfo);
        }
    }

    @PostMapping("/mark-state")
    public ResponseEntity<?> markState(@RequestBody SpotStateDTO info) {
        StateMarkedResponse response = service.markState(info);

        if (!response.marked()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }

    @PostMapping("/unmark-state/{markedStateId}")
    public ResponseEntity<?> deleteStateMark(@PathVariable Long markedStateId) {
        StateUnmarkedResponse response = service.unmarkState(markedStateId);

        if (!response.unmarked()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @GetMapping("/marked")
    public ResponseEntity<?> getMarkedStates() {
        return service.getMarkedStates();
    }
}
