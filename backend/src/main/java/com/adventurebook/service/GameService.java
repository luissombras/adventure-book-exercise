package com.adventurebook.service;

import com.adventurebook.dto.GameView;
import com.adventurebook.model.Book;
import com.adventurebook.model.Game;
import com.adventurebook.model.Option;
import com.adventurebook.model.Section;
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
        Section beginning = book.beginning().orElseThrow();
        Game game = games.save(new Game(UUID.randomUUID().toString(), book.id(), beginning.id()));
        return GameView.of(game, book, beginning);
    }

    public GameView find(String gameId) {
        Game game = game(gameId);
        Book book = bookService.playableBook(game.bookId());
        return GameView.of(game, book, currentSection(game, book));
    }

    public GameView choose(String gameId, int optionIndex) {
        Game game = game(gameId);
        Book book = bookService.playableBook(game.bookId());
        Section current = currentSection(game, book);
        if (current.isEnding()) {
            throw new InvalidChoiceException("The adventure is already over");
        }
        if (optionIndex < 0 || optionIndex >= current.options().size()) {
            throw new InvalidChoiceException("Section " + current.id() + " has no option " + optionIndex);
        }

        Option option = current.options().get(optionIndex);
        Game moved = games.save(game.moveTo(option.gotoId()));
        return GameView.of(moved, book, currentSection(moved, book));
    }

    private Game game(String gameId) {
        return games.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game " + gameId + " not found"));
    }

    private static Section currentSection(Game game, Book book) {
        return book.findSection(game.sectionId()).orElseThrow();
    }
}
