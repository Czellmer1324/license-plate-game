package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.jwt.JwtUtils;
import com.czellmer1324.licenseplategame.mappings.requestobjects.LoginDTO;
import com.czellmer1324.licenseplategame.mappings.requestobjects.SpotStateDTO;
import com.czellmer1324.licenseplategame.mappings.returnobjects.*;
import com.czellmer1324.licenseplategame.repository.SpottedStateRepository;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.mappings.requestobjects.AddUserDTO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SpottedStateRepository spottedRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EntityManager manager;
    private final JwtUtils jwtUtils;

    public CreateUserResponse addUser(AddUserDTO userInfo) {
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

    public String login(LoginDTO info) {
        // Retrieve the user by the userName, may be null
        Optional<User> optionalUser = userRepository.findByUserName(info.userName());
        // check to make sure the user exists in the database with their username
        if (optionalUser.isEmpty()) {
            return "User does not exist";
        }

        //check if password matches
        boolean passMatch = passwordEncoder.matches(info.password(), optionalUser.get().getPassword());

        if (!passMatch) return "Password do not match";
        //create jwt token
        else return jwtUtils.generateTokenFromID(optionalUser.get().getUserId());
    }

    public UserReturnInfo getUserInfo(int id) {
        Optional<User> opUser = userRepository.findById(id);

        if (opUser.isEmpty()) return new UserReturnInfo(-1, null, null, null, null);

        User user = opUser.get();

        return new UserReturnInfo(user.getUserId(), user.getUserName(), user.getFirstName(), user.getLastName(),
                user.getEmail());
    }

    public StateMarkedResponse markState(SpotStateDTO info) {
        if (info.userId() <= 0) {
            return new StateMarkedResponse(false, "Id cannot be less than 0", null);
        }

        if (info.stateCode().isEmpty() || info.stateCode().length() > 2) {
            return new StateMarkedResponse(false, "Improper state code.", null);
        }

        //check to make sure the user exists
        if (!userRepository.existsById(info.userId())) {
            return new StateMarkedResponse(false, "User does not exist", null);
        }

        SpottedStates newSpot = spottedRepository.save(new SpottedStates(manager.getReference(User.class, info.userId()), info.stateCode()));
        return new StateMarkedResponse(true, "State marked", newSpot.getSpottedId());
    }

    public StateUnmarkedResponse unmarkState(Long stateMarkID) {
        if (!spottedRepository.existsById(stateMarkID)) {
            return new StateUnmarkedResponse(false, "Does not exist");
        }

        spottedRepository.deleteById(stateMarkID);
        return new StateUnmarkedResponse(true, "Successfully unmarked state");
    }

    public Optional<Iterable<GetMarkedStatesResponse>> getMarkedStates(int userId) {
        //check to make sure the user exists
        if (!userRepository.existsById(userId)) {
            return Optional.empty();
        }

        return Optional.ofNullable(spottedRepository.findAllByUserUserId(userId));
    }
}
