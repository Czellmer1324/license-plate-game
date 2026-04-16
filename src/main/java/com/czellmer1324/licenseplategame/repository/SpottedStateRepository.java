package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.dto.GetMarkedStatesDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpottedStateRepository extends CrudRepository<SpottedStates, Long> {
    List<GetMarkedStatesDTO> findAllByUserUserId(int userId);
    Boolean existsByUserUserIdAndSpottedId(int userId, Long spottedId);
    Boolean existsByUserUserIdAndStateCode(int userId, String StateCode);
    void deleteAllInBatchByUserUserId(int userId);
}
