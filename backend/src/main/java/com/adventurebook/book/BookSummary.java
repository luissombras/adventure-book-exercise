package com.adventurebook.book;

import java.util.List;

public record BookSummary(
        String id,
        String title,
        String author,
        String description,
        Difficulty difficulty,
        String category,
        int sectionCount,
        boolean valid,
        List<String> errors) {

    static BookSummary of(Book book, ValidationResult validation) {
        String description = book.description() != null
                ? book.description()
                : book.beginning().map(Section::text).orElse(null);
        return new BookSummary(
                book.id(),
                book.title(),
                book.author(),
                description,
                book.difficulty(),
                book.category(),
                book.sections().size(),
                validation.isValid(),
                validation.errors());
    }
}
