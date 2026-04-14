package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.entities.Group;
import com.czellmer1324.licenseplategame.entities.Invite;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final InviteRepository repository;

    protected void createInvite(Group group, User user) {
        repository.save(new Invite(group, user));
    }

    protected boolean checkIfInviteExists(Group group, User user) {
        return repository.existsByGroupAndUser(group, user);
    }
}
