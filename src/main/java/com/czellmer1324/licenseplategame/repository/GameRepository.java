package com.czellmer1324.licenseplategame.repository;

import com.czellmer1324.licenseplategame.repository.entities.Game;
import org.springframework.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<Game, Integer> {
}
