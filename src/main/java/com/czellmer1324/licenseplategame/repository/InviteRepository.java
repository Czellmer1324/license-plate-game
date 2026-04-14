package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.entities.Group;
import com.czellmer1324.licenseplategame.entities.Invite;
import com.czellmer1324.licenseplategame.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface InviteRepository extends CrudRepository<Invite, Long> {
    boolean existsByGroupAndUser(Group group, User user);
}
