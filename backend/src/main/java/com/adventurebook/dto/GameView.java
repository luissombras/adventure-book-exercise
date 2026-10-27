package com.adventurebook.dto;

import com.adventurebook.model.Book;
import com.adventurebook.model.Consequence;
import com.adventurebook.model.Game;
import com.adventurebook.model.GameStatus;
import com.adventurebook.model.Option;
import com.adventurebook.model.Section;

import java.util.List;

public record GameView(
        String id,
        String bookId,
        String bookTitle,
        GameStatus status,
        int health,
        int maxHealth,
        ConsequenceView consequence,
        SectionView section) {

    public record ConsequenceView(String text, int healthChange) {
    }

    public record SectionView(int id, String text, List<String> options) {
    }

    public static GameView of(Game game, Book book) {
        Section section = game.currentSection(book);
        List<String> options = game.status() == GameStatus.IN_PROGRESS
                ? section.options().stream().map(Option::description).toList()
                : List.of();
        return new GameView(
                game.id(),
                book.id(),
                book.title(),
                game.status(),
                game.health(),
                Game.MAX_HEALTH,
                consequenceView(game.lastConsequence()),
                new SectionView(section.id(), section.text(), options));
    }

    private static ConsequenceView consequenceView(Consequence consequence) {
        return consequence == null ? null : new ConsequenceView(consequence.text(), consequence.healthChange());
    }
}
