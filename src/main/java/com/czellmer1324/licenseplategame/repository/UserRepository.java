package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findById(int id);
    boolean existsByUserName(String userName);
    Optional<User> findByUserName(String userName);
    boolean existsByEmail(String email);
}
