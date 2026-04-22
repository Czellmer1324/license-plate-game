package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findById(int id);
    boolean existsByUserName(String userName);
    Optional<User> findByUserName(String userName);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    @Modifying
    @Query("UPDATE User u SET u.numFound = u.numFound + 1 WHERE u.userId = :id")
    void incrementFoundStateCount(@Param("id") int id);
    @Modifying
    @Query("UPDATE User u SET u.numFound = u.numFound - 1 WHERE u.userId = :id")
    void decrementFoundStateCount(@Param("id") int id);
}
