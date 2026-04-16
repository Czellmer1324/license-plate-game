package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.GetInviteDTO;
import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.entities.Group;
import com.czellmer1324.licenseplategame.entities.Invite;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.GroupRepository;
import com.czellmer1324.licenseplategame.repository.InviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final InviteRepository repository;
    private final GroupRepository groupRepository;

    protected void createInvite(Group group, User user) {
        repository.save(new Invite(group, user));
    }

    protected boolean checkIfInviteExists(Group group, User user) {
        return repository.existsByGroupAndUser(group, user);
    }

    protected ServiceResponse acceptInvite(User user, long inviteId) {
        Optional<Invite> opInvite = repository.findById(inviteId);
        if (opInvite.isEmpty()) return new ServiceResponse(Map.of("Message", "This invite does not exist"), HttpStatus.BAD_REQUEST);
        Invite invite = opInvite.get();

        //check to make sure the accepting user is the intended recipient
        if (!invite.getUser().getUserName().equals(user.getUserName())) {
            return new ServiceResponse(Map.of("Message", "This user is the not the intended recipient"), HttpStatus.CONFLICT);
        }

        // add the user to the group
        Group group = invite.getGroup();
        group.getMembers().add(user);

        try {
            groupRepository.save(group);
            repository.deleteById(inviteId);
            return new ServiceResponse(Map.of("Message", "Invite accepted"), HttpStatus.OK);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected List<GetInviteDTO> getInvitesByUserId(int userId) {
        return repository.findAllByUserUserId(userId);
    }
}
