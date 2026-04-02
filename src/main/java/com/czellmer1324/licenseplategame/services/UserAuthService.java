package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private final UserRepository repository;

    public User getUserByID(int id) {
        Optional<User> opUser = repository.findById(id);
        return opUser.orElse(null);
    }
}
