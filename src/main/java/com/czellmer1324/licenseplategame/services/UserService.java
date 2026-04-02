package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.repository.GameRepository;
import com.czellmer1324.licenseplategame.repository.SpottedStateRepository;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import com.czellmer1324.licenseplategame.repository.entities.User;
import com.czellmer1324.licenseplategame.requestobjects.AddUserRequest;
import com.czellmer1324.licenseplategame.returnobjects.CreateUserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SpottedStateRepository spottedRepository;

    public CreateUserResponse addUser(AddUserRequest userInfo) {
        boolean exists = userRepository.existsByUserName(userInfo.userName());
        if (exists) {
            return new CreateUserResponse(false, "A user already exists with that username.");
        }

        //hash the password so it is not stored in plain text
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hashedPass = encoder.encode(userInfo.password());

        try {
            userRepository.save(new User(userInfo.userName(), userInfo.firstName(), userInfo.lastName(), userInfo.email(), hashedPass));
            return new CreateUserResponse(true, "User created");
        } catch (Exception e) {
            return new CreateUserResponse(false, "Something went wrong");
        }

    }
}
