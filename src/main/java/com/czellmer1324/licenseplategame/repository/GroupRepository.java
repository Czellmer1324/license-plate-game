package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.dto.GetGroupsDTO;
import com.czellmer1324.licenseplategame.entities.Group;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends CrudRepository<Group, Long> {
    Optional<Group> findByGroupOwnerUserId(int ownerId);
    List<GetGroupsDTO> findAllByMembersUserId(int userId);
    Optional<Group> findByGroupIdAndMembersUserId(long groupId, int userId);
}
