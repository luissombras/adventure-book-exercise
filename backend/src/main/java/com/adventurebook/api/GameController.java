package com.adventurebook.api;

import com.adventurebook.dto.ChoiceRequest;
import com.adventurebook.dto.GameView;
import com.adventurebook.dto.StartGameRequest;
import com.adventurebook.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameView> start(@Valid @RequestBody StartGameRequest request) {
        GameView game = gameService.start(request.bookId());
        return ResponseEntity.created(URI.create("/api/games/" + game.id())).body(game);
    }

    @GetMapping("/{gameId}")
    public GameView find(@PathVariable String gameId) {
        return gameService.find(gameId);
    }

    @PostMapping("/{gameId}/choices")
    public GameView choose(@PathVariable String gameId, @Valid @RequestBody ChoiceRequest request) {
        return gameService.choose(gameId, request.optionIndex());
    }
}
