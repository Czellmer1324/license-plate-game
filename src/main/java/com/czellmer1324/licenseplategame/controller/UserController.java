package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.dto.*;
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
}
