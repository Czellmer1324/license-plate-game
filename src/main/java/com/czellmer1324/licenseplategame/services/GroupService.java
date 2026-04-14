package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.CreateGroupDTO;
import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.entities.Group;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final Utils util;

    public ServiceResponse createGroup(CreateGroupDTO groupDTO) {
        Optional<User> opUser = util.getUserFromAuth();

        if (opUser.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User is not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        User user = opUser.get();

        try {
            groupRepository.save(new Group(groupDTO.groupName(), user, groupDTO.endDate()));
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
}
