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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    public ResponseEntity<?> login(LoginDTO info) {
        // Ensure username and password are present
        if (info.userName().isEmpty() || info.password().isEmpty()) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(Map.of("Message", "Username or password missing"));
        }

        // Retrieve the user by the userName, may be null
        Optional<User> optionalUser = userRepository.findByUserName(info.userName());
        // check to make sure the user exists in the database with their username
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Message", "User does not exist"));
        }

        //check if password matches
        boolean passMatch = passwordEncoder.matches(info.password(), optionalUser.get().getPassword());

        if (!passMatch) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Message", "Incorrect password"));
        }

        //create jwt token
        else {
            String token = jwtUtils.generateTokenFromID(optionalUser.get().getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("Token", token));
        }
    }

    public ResponseEntity<?> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User user) {
                Map<String, String> response = Map.of("User Name", user.getUserName(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail());
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Message", "User not authenticated"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Message", "User not authenticated"));
        }
    }

    public ResponseEntity<?> markState(SpotStateDTO info) {
        Optional<Integer> opId = getUserIDFromAuth();

        if (opId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Message", "User not authenticated"));
        }

        if (info.stateCode().length() != 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Message", "Improper state code"));
        }

        // Make sure the state is not already marked
        if (spottedRepository.existsByUserUserIdAndStateCode(opId.get(), info.stateCode())) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(Map.of("Message", "State is already marked"));
        }

        SpottedStates newSpot = spottedRepository.save(new SpottedStates(manager.getReference(User.class, opId.get()), info.stateCode()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", newSpot.getSpottedId()));
    }

    // Updated this
    public ResponseEntity<?> unmarkState(Long stateMarkID) {
        if (!spottedRepository.existsById(stateMarkID)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Message", "Spotted ID does not exist"));
        }

        spottedRepository.deleteById(stateMarkID);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("Message", "Unmarked successfully"));
    }

    // Updated to new way already using JWT
    public ResponseEntity<?> getMarkedStates() {
        Optional<Integer> opId = getUserIDFromAuth();
        Map<String, String> response = new HashMap<>();
        if (opId.isEmpty()) {
            response.put("Message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Iterable<GetMarkedStatesResponse> spottedStates = spottedRepository.findAllByUserUserId(opId.get());

        return ResponseEntity.status(HttpStatus.OK).body(spottedStates);
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
