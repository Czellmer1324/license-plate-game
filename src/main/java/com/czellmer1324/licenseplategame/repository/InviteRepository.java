package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.dto.GetInviteDTO;
import com.czellmer1324.licenseplategame.entities.Group;
import com.czellmer1324.licenseplategame.entities.Invite;
import com.czellmer1324.licenseplategame.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InviteRepository extends CrudRepository<Invite, Long> {
    boolean existsByGroupAndUser(Group group, User user);
    List<GetInviteDTO> findAllByUserUserId(int userId);
    void deleteAllInBatchByGroupGroupId(long groupId);
}
