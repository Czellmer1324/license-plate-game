package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.dto.GetMarkedStatesDTO;
import org.springframework.data.repository.CrudRepository;

public interface SpottedStateRepository extends CrudRepository<SpottedStates, Long> {
    Iterable<GetMarkedStatesDTO> findAllByUserUserId(int userId);
    Boolean existsByUserUserIdAndStateCode(int userId, String stateCode);
    void deleteByUserUserIdAndStateCode(int userId, String stateCode);
}
