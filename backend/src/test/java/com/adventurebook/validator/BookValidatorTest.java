package com.adventurebook.validator;

import com.adventurebook.model.Book;
import com.adventurebook.model.Difficulty;
import com.adventurebook.model.Option;
import com.adventurebook.model.Section;
import com.adventurebook.model.SectionType;
import com.adventurebook.repository.BookLoader;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.adventurebook.model.SectionType.BEGIN;
import static com.adventurebook.model.SectionType.END;
import static com.adventurebook.model.SectionType.NODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class BookValidatorTest {

    private final BookValidator validator = new BookValidator();

    @Test
    void validBookHasNoErrors() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 2, 3),
                section(2, NODE, 3),
                section(3, END)));

        assertThat(result.isValid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void bookWithoutBeginningIsInvalid() {
        ValidationResult result = validator.validate(book(
                section(1, NODE, 2),
                section(2, END)));

        assertThat(result.errors()).containsExactly("Book has no beginning section");
    }

    @Test
    void bookWithMoreThanOneBeginningIsInvalid() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 3),
                section(2, BEGIN, 3),
                section(3, END)));

        assertThat(result.errors()).containsExactly("Book has more than one beginning section (sections 1, 2)");
    }

    @Test
    void bookWithoutEndingIsInvalid() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 2),
                section(2, NODE, 1)));

        assertThat(result.errors()).containsExactly("Book has no ending section");
    }

    @Test
    void optionLeadingToUnknownSectionIsInvalid() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 2, 999),
                section(2, END)));

        assertThat(result.errors()).containsExactly("Section 1 has an option leading to unknown section 999");
    }

    @Test
    void reachableNonEndingSectionWithoutOptionsIsInvalid() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 2, 3),
                section(2, NODE),
                section(3, END)));

        assertThat(result.errors()).containsExactly("Section 2 is not an ending but has no options");
    }

    @Test
    void unreachableSectionWithoutOptionsIsIgnored() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 2),
                section(2, END),
                section(666, NODE)));

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void duplicateSectionIdIsInvalid() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 2),
                section(2, END),
                section(2, NODE, 1),
                section(2, END)));

        assertThat(result.errors()).containsExactly("Section 2 is defined more than once");
    }

    @Test
    void sectionsReachedByManyPathsAndCyclesAreCheckedOnce() {
        ValidationResult result = validator.validate(book(
                section(1, BEGIN, 2, 3, 4),
                section(2, NODE, 3, 4, 1),
                section(3, NODE, 4, 2),
                section(4, NODE)));

        assertThat(result.errors()).containsExactly(
                "Book has no ending section",
                "Section 4 is not an ending but has no options");
    }

    @Test
    void reportsEveryProblemAtOnce() {
        ValidationResult result = validator.validate(book(
                section(1, NODE, 42),
                section(2, NODE)));

        assertThat(result.errors()).containsExactly(
                "Book has no beginning section",
                "Book has no ending section",
                "Section 1 has an option leading to unknown section 42");
    }

    @Test
    void bundledBooksAreValidatedAsExpected() {
        List<Book> books = new BookLoader(JsonMapper.builder().build(), "classpath:books/*.json").loadAll();

        Map<String, List<String>> errorsByBook = books.stream()
                .collect(Collectors.toMap(Book::id, book -> validator.validate(book).errors()));

        assertThat(errorsByBook).containsOnly(
                entry("crystal-caverns", List.of("Section 666 is not an ending but has no options")),
                entry("pirates-jade-sea", List.of(
                        "Section 1 has an option leading to unknown section 999",
                        "Section 666 is not an ending but has no options")),
                entry("the-prisoner", List.of()));
    }

    private static Book book(Section... sections) {
        return new Book("test", "Test", "Tester", Difficulty.EASY, Arrays.asList(sections));
    }

    private static Section section(int id, SectionType type, int... gotoIds) {
        List<Option> options = Arrays.stream(gotoIds)
                .mapToObj(gotoId -> new Option("Go to " + gotoId, gotoId, null))
                .toList();
        return new Section(id, "Section " + id, type, options);
    }
}
