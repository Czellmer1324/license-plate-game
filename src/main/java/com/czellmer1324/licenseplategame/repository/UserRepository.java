package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.repository.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    User findById(int id);
    boolean existsByUserName(String userName);
}
