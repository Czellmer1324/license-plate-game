package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.*;
import com.czellmer1324.licenseplategame.jwt.JwtUtils;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import com.czellmer1324.licenseplategame.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final Utils utils;
    private final InviteService inviteService;

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
        if (info.email().isEmpty() || info.password().isEmpty()) {
            return new ServiceResponse(Map.of("Message", "Username or password missing"), HttpStatus.UNPROCESSABLE_CONTENT);
        }

        // Retrieve the user by the userName, may be null
        Optional<User> optionalUser = userRepository.findByEmail(info.email());
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
        Optional<User> opUser = utils.getUserFromAuth();

        if (opUser.isPresent()) {
            User user = opUser.get();
            Map<String, String> response = Map.of("User Name", user.getUserName(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "email", user.getEmail());
            return new ServiceResponse(response, HttpStatus.OK);
        } else {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }
    }

    public ServiceResponse acceptInvite(InviteResponseDTO info) {
        // get the user
        Optional<User> opUser = utils.getUserFromAuth();
        if (opUser.isEmpty()) return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        return inviteService.acceptInvite(opUser.get(), info.inviteId());
    }

    public ServiceResponse getInvites() {
        // get the user from auth
        Optional<User> opUser = utils.getUserFromAuth();
        if (opUser.isEmpty()) return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        User user = opUser.get();
        // retrieve their invites
        List<GetInviteDTO> invites = inviteService.getInvitesByUserId(user.getUserId());
        // return the invites
        return new ServiceResponse(invites, HttpStatus.OK);
    }

    public ServiceResponse declineInvite(long inviteId) {
        // get the user
        Optional<User> opUser = utils.getUserFromAuth();
        if (opUser.isEmpty()) return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        return inviteService.declineInvite(opUser.get(), inviteId);
    }

    protected Optional<User> getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }
}
