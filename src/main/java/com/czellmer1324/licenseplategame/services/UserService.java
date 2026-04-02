package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.repository.GameRepository;
import com.czellmer1324.licenseplategame.repository.SpottedStateRepository;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.mappings.requestobjects.AddUserRequest;
import com.czellmer1324.licenseplategame.mappings.returnobjects.CreateUserResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SpottedStateRepository spottedRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GameRepository gameRepository, SpottedStateRepository spot,
                       BCryptPasswordEncoder passwordEncoder) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.spottedRepository = spot;
        this.passwordEncoder = passwordEncoder;
    }

    public CreateUserResponse addUser(AddUserRequest userInfo) {
        boolean exists = userRepository.existsByUserName(userInfo.userName());
        if (exists) {
            return new CreateUserResponse(false, "A user already exists with that username.");
        }

        //hash the password so it is not stored in plain text
        String hashedPass = passwordEncoder.encode(userInfo.password());

        try {
            userRepository.save(new User(userInfo.userName(), userInfo.firstName(), userInfo.lastName(), userInfo.email(), hashedPass));
            return new CreateUserResponse(true, "User created");
        } catch (Exception e) {
            return new CreateUserResponse(false, "Something went wrong");
        }

    }
}
