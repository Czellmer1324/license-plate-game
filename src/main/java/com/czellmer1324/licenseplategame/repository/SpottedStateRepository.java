package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.mappings.returnobjects.GetMarkedStatesResponse;
import org.springframework.data.repository.CrudRepository;

public interface SpottedStateRepository extends CrudRepository<SpottedStates, Long> {
    Iterable<GetMarkedStatesResponse> findAllByUserUserId(int userId);
}
