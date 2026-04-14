package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.dto.SpotStateDTO;
import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.SpottedStateRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final SpottedStateRepository gameRepository;
    private final EntityManager manager;
    private final Utils util;

    public ServiceResponse markState(SpotStateDTO info) {
        Optional<Integer> opId = util.getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        if (info.stateCode().length() != 2) {
            return new ServiceResponse(Map.of("Message", "Improper state code"), HttpStatus.BAD_REQUEST);
        }

        // Make sure the state is not already marked
        if (gameRepository.existsByUserUserIdAndStateCode(opId.get(), info.stateCode())) {
            return new ServiceResponse(Map.of("Message", "State is already marked"), HttpStatus.ALREADY_REPORTED);
        }

        SpottedStates newSpot = gameRepository.save(new SpottedStates(manager.getReference(User.class, opId.get()), info.stateCode()));
        return new ServiceResponse(Map.of( "spottedId", newSpot.getSpottedId(), "stateCode", newSpot.getStateCode()), HttpStatus.CREATED);
    }

    public ServiceResponse unmarkState(Long id) {
        Optional<Integer> opId = util.getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        if (!gameRepository.existsByUserUserIdAndSpottedId(opId.get(), id)) {
            return new ServiceResponse(Map.of("Message", "State not spotted for this user"), HttpStatus.NOT_FOUND);
        }

        gameRepository.deleteById(id);
        return new ServiceResponse(Map.of("Message", "Unmarked successfully"), HttpStatus.OK);
    }

    public ServiceResponse getMarkedStates() {
        Optional<Integer> opId = util.getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        return new ServiceResponse(gameRepository.findAllByUserUserId(opId.get()), HttpStatus.OK);

    }

    public ServiceResponse unmarkAll() {
        Optional<Integer> opId = util.getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        gameRepository.deleteAllByUserUserId(opId.get());
        return new ServiceResponse(Map.of("Message", "States unmarked successfully"), HttpStatus.OK);
    }
}
