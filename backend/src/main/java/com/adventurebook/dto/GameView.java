package com.adventurebook.dto;

import com.adventurebook.model.Book;
import com.adventurebook.model.Game;
import com.adventurebook.model.GameStatus;
import com.adventurebook.model.Option;
import com.adventurebook.model.Section;

import java.util.List;

public record GameView(String id, String bookId, String bookTitle, GameStatus status, SectionView section) {

    public record SectionView(int id, String text, List<String> options) {
    }

    public static GameView of(Game game, Book book, Section section) {
        GameStatus status = section.isEnding() ? GameStatus.FINISHED : GameStatus.IN_PROGRESS;
        List<String> options = section.options().stream().map(Option::description).toList();
        return new GameView(game.id(), book.id(), book.title(), status,
                new SectionView(section.id(), section.text(), options));
    }
}
