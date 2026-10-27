package com.adventurebook.repository;

import com.adventurebook.model.Game;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public Optional<Game> findById(String id) {
        return Optional.ofNullable(games.get(id));
    }

    public Game save(Game game) {
        games.put(game.id(), game);
        return game;
    }
}
