package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.mappings.requestobjects.AddUserRequest;
import com.czellmer1324.licenseplategame.mappings.requestobjects.SpotStateRequest;
import com.czellmer1324.licenseplategame.mappings.returnobjects.CreateUserResponse;
import com.czellmer1324.licenseplategame.mappings.returnobjects.StateMarkedResponse;
import com.czellmer1324.licenseplategame.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public CreateUserResponse addUser(@RequestBody AddUserRequest userInfo) {
        return service.addUser(userInfo);
    }

    @PostMapping("/mark-state")
    public ResponseEntity<?> markState(@RequestBody SpotStateRequest info) {
        StateMarkedResponse response = service.markState(info);

        if (!response.marked()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }
}
