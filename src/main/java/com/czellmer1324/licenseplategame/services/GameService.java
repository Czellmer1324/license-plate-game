package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.dto.GetMarkedStatesDTO;
import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.dto.SpotStateDTO;
import com.czellmer1324.licenseplategame.entities.SpottedStates;
import com.czellmer1324.licenseplategame.entities.User;
import com.czellmer1324.licenseplategame.repository.SpottedStateRepository;
import com.czellmer1324.licenseplategame.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {
    private final SpottedStateRepository gameRepository;
    private final EntityManager manager;
    private final UserRepository userRepository;
    private final Utils util;

    public ServiceResponse markState(SpotStateDTO info) {
        Optional<User> opUser = util.getUserFromAuth();

        if (opUser.isEmpty()) {
            return util.noAuthResponse();
        }

        User user = opUser.get();

        if (info.stateCode().length() != 2) {
            return new ServiceResponse(Map.of("Message", "Improper state code"), HttpStatus.BAD_REQUEST);
        }

        // Make sure the state is not already marked
        if (gameRepository.existsByUserUserIdAndStateCode(user.getUserId(), info.stateCode())) {
            return new ServiceResponse(Map.of("Message", "State is already marked"), HttpStatus.ALREADY_REPORTED);
        }

        try {
            SpottedStates newSpot = gameRepository.save(new SpottedStates(manager.getReference(User.class, user.getUserId()), info.stateCode()));
            user.addFound();
            userRepository.save(user);

            return new ServiceResponse(Map.of( "spottedId", newSpot.getSpottedId(), "stateCode", newSpot.getStateCode()), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ServiceResponse unmarkState(Long id) {
        Optional<User> opUser = util.getUserFromAuth();

        if (opUser.isEmpty()) {
            return util.noAuthResponse();
        }

        User user = opUser.get();

        if (!gameRepository.existsByUserUserIdAndSpottedId(user.getUserId(), id)) {
            return new ServiceResponse(Map.of("Message", "State not spotted for this user"), HttpStatus.NOT_FOUND);
        }

        try {
            gameRepository.deleteById(id);
            user.subtractFound();
            userRepository.save(user);
            return new ServiceResponse(Map.of("Message", "Unmarked successfully"), HttpStatus.OK);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ServiceResponse getMarkedStates() {
        Optional<Integer> opId = util.getUserIDFromAuth();

        if (opId.isEmpty()) {
            return new ServiceResponse(Map.of("Message", "User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        return new ServiceResponse(gameRepository.findAllByUserUserId(opId.get()), HttpStatus.OK);

    }

    @Transactional
    public ServiceResponse unmarkAll() {
        Optional<User> opUser = util.getUserFromAuth();

        if (opUser.isEmpty()) {
            return util.noAuthResponse();
        }

        User user = opUser.get();

        try {
            gameRepository.deleteAllInBatchByUserUserId(user.getUserId());
            user.setNumFound(0);
            userRepository.save(user);
            return new ServiceResponse(Map.of("Message", "States unmarked successfully"), HttpStatus.OK);
        } catch (Exception e) {
            return new ServiceResponse(Map.of("Message", "Something went wrong please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected List<GetMarkedStatesDTO> getMarkedStatesById(int userId) {
        return gameRepository.findAllByUserUserId(userId);
    }
}
