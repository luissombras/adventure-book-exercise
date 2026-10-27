package com.adventurebook.dto;

import com.adventurebook.model.Book;
import com.adventurebook.model.Difficulty;
import com.adventurebook.model.Section;
import com.adventurebook.model.SectionType;
import com.adventurebook.validator.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookSummaryTest {

    @Test
    void summarisesABookWithItsOpeningTextAsDescription() {
        Book book = new Book("b", "Title", "Author", Difficulty.HARD, List.of(
                new Section(5, "Middle", SectionType.NODE, List.of()),
                new Section(1, "Opening", SectionType.BEGIN, List.of()),
                new Section(9, "End", SectionType.END, List.of())));

        BookSummary summary = BookSummary.of(book, new ValidationResult(List.of()));

        assertThat(summary).isEqualTo(new BookSummary("b", "Title", "Author", "Opening", Difficulty.HARD, 3, true, List.of()));
    }

    @Test
    void bookWithoutBeginningHasNoDescriptionAndKeepsItsErrors() {
        Book book = new Book("b", "Title", "Author", null, List.of(new Section(1, "Text", SectionType.NODE, List.of())));

        BookSummary summary = BookSummary.of(book, new ValidationResult(List.of("Book has no beginning section")));

        assertThat(summary.description()).isNull();
        assertThat(summary.difficulty()).isNull();
        assertThat(summary.valid()).isFalse();
        assertThat(summary.errors()).containsExactly("Book has no beginning section");
    }
}
