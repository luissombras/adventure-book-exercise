package com.adventurebook.service;

import com.adventurebook.dto.GameView;
import com.adventurebook.model.Book;
import com.adventurebook.model.Game;
import com.adventurebook.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameService {

    private final BookService bookService;
    private final GameRepository games;

    public GameService(BookService bookService, GameRepository games) {
        this.bookService = bookService;
        this.games = games;
    }

    public GameView start(String bookId) {
        Book book = bookService.playableBook(bookId);
        Game game = games.save(Game.start(UUID.randomUUID().toString(), book));
        return GameView.of(game, book);
    }

    public GameView find(String gameId) {
        Game game = game(gameId);
        return GameView.of(game, bookService.playableBook(game.bookId()));
    }

    public GameView choose(String gameId, int optionIndex) {
        Book book = bookService.playableBook(game(gameId).bookId());
        Game game = games.update(gameId, current -> current.choose(book, optionIndex))
                .orElseThrow(() -> notFound(gameId));
        return GameView.of(game, book);
    }

    private Game game(String gameId) {
        return games.findById(gameId).orElseThrow(() -> notFound(gameId));
    }

    private static NotFoundException notFound(String gameId) {
        return new NotFoundException("Game " + gameId + " not found");
    }
}
