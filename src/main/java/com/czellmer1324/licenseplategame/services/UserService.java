package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.*;
import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.jwt.JwtUtils;
import com.czellmer1324.licenseplategame.repository.SpottedStateRepository;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import com.czellmer1324.licenseplategame.entities.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SpottedStateRepository spottedRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EntityManager manager;
    private final JwtUtils jwtUtils;

    public ServiceResponse addUser(AddUserDTO userInfo) {
        boolean userNameExists = userRepository.existsByUserName(userInfo.userName());
        if (userNameExists) {
            return new ServiceResponse(Map.of("Message", "User name already exists"), HttpStatus.CONFLICT);
        }

        boolean emailExists =  userRepository.existsByEmail(userInfo.email());
        if (emailExists) {
            return new ServiceResponse(Map.of("Message", "Email already exists"), HttpStatus.CONFLICT);
        }

        //hash the password so it is not stored in plain text
        String hashedPass = passwordEncoder.encode(userInfo.password());

        try {
            userRepository.save(new User(userInfo.userName(), userInfo.firstName(), userInfo.lastName(), userInfo.email(), hashedPass));
            return new ServiceResponse(Map.of("Message", "User created successfully"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ServiceResponse login(LoginDTO info) {
        // Ensure username and password are present
        if (info.userName().isEmpty() || info.password().isEmpty()) {
            return new ServiceResponse(Map.of("Message", "Username or password missing"), HttpStatus.UNPROCESSABLE_CONTENT);
        }

        // Retrieve the user by the userName, may be null
        Optional<User> optionalUser = userRepository.findByUserName(info.userName());
        // check to make sure the user exists in the database with their username
        if (optionalUser.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "Invalid Credentials"), HttpStatus.UNAUTHORIZED);
        }

        //check if password matches
        boolean passMatch = passwordEncoder.matches(info.password(), optionalUser.get().getPassword());

        if (!passMatch) {
            return new ServiceResponse(Map.of("Message", "Invalid Credentials"), HttpStatus.UNAUTHORIZED);
        }

        //create jwt token
        else {
            String token = jwtUtils.generateTokenFromID(optionalUser.get().getUserId());
            return new ServiceResponse(Map.of("Token", token), HttpStatus.OK);
        }
    }

    public ServiceResponse getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User user) {
                Map<String, String> response = Map.of("User Name", user.getUserName(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail());
                return new ServiceResponse(response, HttpStatus.OK);
            } else {
                return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }
    }

    public ServiceResponse markState(SpotStateDTO info) {
        Optional<Integer> opId = getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        if (info.stateCode().length() != 2) {
            return new ServiceResponse(Map.of("Message", "Improper state code"), HttpStatus.BAD_REQUEST);
        }

        // Make sure the state is not already marked
        if (spottedRepository.existsByUserUserIdAndStateCode(opId.get(), info.stateCode())) {
            return new ServiceResponse(Map.of("Message", "State is already marked"), HttpStatus.ALREADY_REPORTED);
        }

        SpottedStates newSpot = spottedRepository.save(new SpottedStates(manager.getReference(User.class, opId.get()), info.stateCode()));
        return new ServiceResponse(Map.of( "spottedId", newSpot.getSpottedId(), "stateCode", newSpot.getStateCode()), HttpStatus.CREATED);
    }

    public ServiceResponse unmarkState(Long id) {
        Optional<Integer> opId = getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        if (!spottedRepository.existsByUserUserIdAndSpottedId(opId.get(), id)) {
            return new ServiceResponse(Map.of("Message", "State not spotted for this user"), HttpStatus.NOT_FOUND);
        }

       spottedRepository.deleteById(id);
        return new ServiceResponse(Map.of("Message", "Unmarked successfully"), HttpStatus.OK);
    }

    public ServiceResponse getMarkedStates() {
        Optional<Integer> opId = getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        return new ServiceResponse(spottedRepository.findAllByUserUserId(opId.get()), HttpStatus.OK);

    }

    private Optional<Integer> getUserIDFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof User) {
                return Optional.of(((User) principal).getUserId());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
