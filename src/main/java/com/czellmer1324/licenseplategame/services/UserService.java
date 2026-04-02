package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.mappings.requestobjects.SpotStateRequest;
import com.czellmer1324.licenseplategame.mappings.returnobjects.StateMarkedResponse;
import com.czellmer1324.licenseplategame.repository.SpottedStateRepository;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.mappings.requestobjects.AddUserRequest;
import com.czellmer1324.licenseplategame.mappings.returnobjects.CreateUserResponse;
import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SpottedStateRepository spottedRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EntityManager manager;

    public UserService(UserRepository userRepository, SpottedStateRepository spot,
                       BCryptPasswordEncoder passwordEncoder, EntityManager manager) {
        this.userRepository = userRepository;
        this.spottedRepository = spot;
        this.passwordEncoder = passwordEncoder;
        this.manager = manager;
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

    public StateMarkedResponse markState(SpotStateRequest info) {
        if (info.userId() <= 0) {
            return new StateMarkedResponse(false, "Id cannot be less than 0");
        }

        if (info.stateCode().isEmpty() || info.stateCode().length() > 2) {
            return new StateMarkedResponse(false, "Improper state code.");
        }

        //check to make sure the user exists
        if (!userRepository.existsById(info.userId())) {
            return new StateMarkedResponse(false, "User does not exist");
        }

        spottedRepository.save(new SpottedStates(manager.getReference(User.class, info.userId()), info.stateCode()));
        return new StateMarkedResponse(true, "State marked");
    }
}
