package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.CreateGroupDTO;
import com.czellmer1324.licenseplategame.dto.InviteDTO;
import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.entities.Group;
import com.czellmer1324.licenseplategame.entities.Invite;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final Utils util;
    private final UserService userService;
    private final InviteService inviteService;

    public ServiceResponse createGroup(CreateGroupDTO groupDTO) {
        Optional<User> opUser = util.getUserFromAuth();

        if (opUser.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User is not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        User user = opUser.get();

        try {
            Group group = new Group(groupDTO.groupName(), user, List.of(user), groupDTO.endDate());
            groupRepository.save(group);
            return new ServiceResponse(Map.of("Message", "Group created successfully"), HttpStatus.CREATED);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            return new ServiceResponse(Map.of("Message", "User already owns a group"), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ServiceResponse deleteGroup() {
        Optional<Integer> opId = util.getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User is not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        int userId = opId.get();

        Optional<Group> opGroup = groupRepository.findByGroupOwnerUserId(userId);

        if (opGroup.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "This user does not own a group"), HttpStatus.BAD_REQUEST);
        }

        try {
            groupRepository.delete(opGroup.get());
            return new ServiceResponse(Map.of("Message", "Group delete successfully"), HttpStatus.OK);
        } catch (Exception e) {
            IO.println(e.getMessage());
            IO.println(opGroup.get().getGroupId());
            return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResponse inviteUserToGroup(long groupId, InviteDTO inviteDTO) {
        // make sure the sending user is authenticated
        Optional<User> opUser = util.getUserFromAuth();
        if (opUser.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User is not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        User groupOwner = opUser.get();
        // make sure the sending user owns the group
        // make sure the group exists
        Optional<Group> opGroup = groupRepository.findById(groupId);
        if (opGroup.isEmpty())
            return new ServiceResponse(Map.of("Message", "This group does not exist"), HttpStatus.BAD_REQUEST);
        if (opGroup.get().getGroupOwner().getUserId() != groupOwner.getUserId()) {
            return new ServiceResponse(Map.of("Message", "This user does not own this group"), HttpStatus.UNAUTHORIZED);
        }
        Group group = opGroup.get();
        // make sure the user they are trying to invite exists
        Optional<User> opInvitee = userService.getUserByUserName(inviteDTO.userName());
        if (opInvitee.isEmpty())
            return new ServiceResponse(Map.of("Message", "This user does not exist"), HttpStatus.BAD_REQUEST);
        // check to make sure the user is not already part of the group
        if (group.getMembers().contains(opInvitee.get()))
            return new ServiceResponse(Map.of("Message", "User is already a part of this group"), HttpStatus.CONFLICT);

        //check to make sure the invite does not already
        if (inviteService.checkIfInviteExists(group, opInvitee.get()))
            return new ServiceResponse(Map.of("Message", "This user has already been invited"), HttpStatus.CONFLICT);
        // send info to invite service
        inviteService.createInvite(group, opInvitee.get());
        return new ServiceResponse(Map.of("Message", "User was invited"), HttpStatus.OK);
    }
}
