package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.*;
import com.czellmer1324.licenseplategame.entities.Group;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final Utils util;
    private final UserService userService;
    private final InviteService inviteService;
    private final GameService gameService;

    public ServiceResponse createGroup(CreateGroupDTO groupDTO) {
        Optional<User> opUser = util.getUserFromAuth();

        if (opUser.isEmpty()) return util.noAuthResponse();

        User user = opUser.get();

        try {
            Group group = new Group(groupDTO.groupName(), user, List.of(user), groupDTO.endDate());
            Group savedGroup = groupRepository.save(group);
            GetGroupsDTO groupReturn = new GetGroupsDTO(savedGroup.getGroupName(), user.getUserName(), savedGroup.getGroupId(), savedGroup.getEndDate());
            return new ServiceResponse(Map.of("Message", "Group created successfully", "Group", groupReturn), HttpStatus.CREATED);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            return new ServiceResponse(Map.of("Message", "User already owns a group"), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ServiceResponse deleteGroup() {
        Optional<Integer> opId = util.getUserIDFromAuth();

        if (opId.isEmpty()) return util.noAuthResponse();

        int userId = opId.get();

        Optional<Group> opGroup = groupRepository.findByGroupOwnerUserId(userId);

        if (opGroup.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "This user does not own a group"), HttpStatus.BAD_REQUEST);
        }

        try {
            Group group = opGroup.get();
            inviteService.deleteAllByGroupId(group.getGroupId());
            groupRepository.deleteById(group.getGroupId());
            return new ServiceResponse(Map.of("Message", "Group delete successfully"), HttpStatus.OK);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResponse inviteUserToGroup(long groupId, InviteDTO inviteDTO) {
        // make sure the sending user is authenticated
        Optional<User> opUser = util.getUserFromAuth();
        if (opUser.isEmpty()) return util.noAuthResponse();

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
            return new ServiceResponse(Map.of("Message", "This user does not exist"), HttpStatus.NOT_FOUND);
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

    public ServiceResponse getUserGroups() {
        Optional<Integer> opId = util.getUserIDFromAuth();
        if (opId.isEmpty()) return util.noAuthResponse();
        List<GetGroupsDTO> groups = groupRepository.findAllByMembersUserId(opId.get());
        return new ServiceResponse(groups, HttpStatus.OK);
    }

    public ServiceResponse getGroupInfo(long groupId) {
        // get the user that sent the request
        Optional<User> opUser = util.getUserFromAuth();
        if (opUser.isEmpty()) return util.noAuthResponse();
        User user = opUser.get();
        // get the group
        Optional<Group> opGroup = groupRepository.findByGroupIdAndMembersUserId(groupId, user.getUserId());
        if (opGroup.isEmpty()) return new ServiceResponse(Map.of("Message", "This group does not exist or not part of group"), HttpStatus.BAD_REQUEST);
        Group group = opGroup.get();
        List<User> members = group.getMembers();

        // convert the group to DTO so that it protects users sensitive info
        GetGroupDTO safeReturn = new GetGroupDTO(group.getGroupName(), group.getGroupOwner().getUserName(), new ArrayList<>(), group.getEndDate(), groupId);

        for (User member : members) {
            safeReturn.members().add(new SafeUserDTO(member.getUserName(), member.getUserId(), member.getNumFound()));
        }

        return new ServiceResponse(safeReturn, HttpStatus.OK);
    }

    public ServiceResponse changeEndDate(ZonedDateTime newDate) {
        Optional<Integer> opId = util.getUserIDFromAuth();
        if (opId.isEmpty()) return util.noAuthResponse();
        int id = opId.get();

        // get the group
        Optional<Group> opGroup = groupRepository.findByGroupOwnerUserId(id);
        if (opGroup.isEmpty()) return new ServiceResponse(Map.of("Message", "This user does not own a group"), HttpStatus.BAD_REQUEST);
        Group group = opGroup.get();

        group.setEndDate(newDate);

        try {
            groupRepository.save(group);
            Map<String, Object> response = new HashMap<>();
            response.put("Message", "End date updated");
            response.put("NewDate", newDate);
            return new ServiceResponse(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ServiceResponse removeUser(String userName) {
        // Get the person sending the request
        Optional<Integer> opId = util.getUserIDFromAuth();
        if (opId.isEmpty()) return util.noAuthResponse();
        int id = opId.get();
        // get the group they own
        Optional<Group> opGroup = groupRepository.findByGroupOwnerUserId(id);
        if (opGroup.isEmpty()) return new ServiceResponse(Map.of("Message", "This user does not own a group"), HttpStatus.NOT_FOUND);
        Group group = opGroup.get();
        // check to make sure the user they are trying to remove is part of the group
        for (User user : group.getMembers()) {
            if (user.getUserName().equals(userName)) {
                group.getMembers().remove(user);
                try {
                    groupRepository.save(group);
                    return new ServiceResponse(Map.of("Message", "User removed from group"), HttpStatus.OK);
                } catch (Exception e) {
                    return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }

        return new ServiceResponse(Map.of("Message", "User is not part of this group"), HttpStatus.CONFLICT);
    }

    public ServiceResponse getMemberFoundStates(int userId, long groupId) {
        Optional<Integer> opId = util.getUserIDFromAuth();
        if (opId.isEmpty()) return util.noAuthResponse();
        int requesterId = opId.get();

        if (requesterId == userId) {
            return new ServiceResponse(Map.of("Message", "User trying to view their own map"), HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<Group> opGroup = groupRepository.findById(groupId);
            if (opGroup.isEmpty()) return new ServiceResponse(Map.of("Message", "This group does not exist"), HttpStatus.NOT_FOUND);
            Group group = opGroup.get();
            List<User> members = group.getMembers();
            boolean requesterPartOf = false;
            boolean targetPartOfGroup = false;

            for (User user : members) {
                if (user.getUserId() == requesterId) {
                    requesterPartOf = true;
                }

                if (user.getUserId() == userId) {
                    targetPartOfGroup = true;
                }
            }

            if (!requesterPartOf) {
                return new ServiceResponse(Map.of("Message", "Requester is not part of this group"), HttpStatus.NOT_FOUND);
            }

            if (!targetPartOfGroup) {
                return new ServiceResponse(Map.of("Message", "Target user is not part of this group"), HttpStatus.NOT_FOUND);
            }

            return new ServiceResponse(gameService.getMarkedStatesById(userId), HttpStatus.OK);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResponse leaveGroup(long groupId) {
        // Get the person sending the request
        Optional<Integer> opId = util.getUserIDFromAuth();
        if (opId.isEmpty()) return util.noAuthResponse();
        int id = opId.get();

        Optional<Group> opGroup = groupRepository.findById(groupId);
        if (opGroup.isEmpty()) return new ServiceResponse(Map.of("Message", "This group does not exist"), HttpStatus.NOT_FOUND);
        Group group = opGroup.get();

        // check to make sure the user they are trying to remove is part of the group
        for (User user : group.getMembers()) {
            if (user.getUserId() == id) {
                group.getMembers().remove(user);
                try {
                    groupRepository.save(group);
                    return new ServiceResponse(Map.of("Message", "User has left the group"), HttpStatus.OK);
                } catch (Exception e) {
                    return new ServiceResponse(Map.of("Message", "Something went wrong, try again"), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }

        return new ServiceResponse(Map.of("Message", "User is not part of this group"), HttpStatus.NOT_FOUND);
    }
}
