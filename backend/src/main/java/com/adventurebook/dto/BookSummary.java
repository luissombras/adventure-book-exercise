package com.adventurebook.dto;

import com.adventurebook.model.Book;
import com.adventurebook.model.Difficulty;
import com.adventurebook.model.Section;
import com.adventurebook.validator.ValidationResult;

import java.util.List;

public record BookSummary(
        String id,
        String title,
        String author,
        String description,
        Difficulty difficulty,
        int sectionCount,
        boolean valid,
        List<String> errors) {

    public static BookSummary of(Book book, ValidationResult validation) {
        return new BookSummary(
                book.id(),
                book.title(),
                book.author(),
                book.beginning().map(Section::text).orElse(null),
                book.difficulty(),
                book.sections().size(),
                validation.isValid(),
                validation.errors());
    }
}
