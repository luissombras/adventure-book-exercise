package com.adventurebook.repository;

import com.adventurebook.model.Book;
import com.adventurebook.model.Consequence;
import com.adventurebook.model.ConsequenceType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.UncheckedIOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookLoaderTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void loadsBundledBooksAndSkipsTheEmptyOne() {
        List<Book> books = new BookLoader(jsonMapper, "classpath:books/*.json").loadAll();

        assertThat(books).extracting(Book::id)
                .containsExactlyInAnyOrder("crystal-caverns", "pirates-jade-sea", "the-prisoner");
    }

    @Test
    void acceptsSectionIdsWrittenAsStrings() {
        Book prisoner = loadBundled("the-prisoner");

        assertThat(prisoner.findSection(1000))
                .hasValueSatisfying(section -> assertThat(section.isEnding()).isTrue());
    }

    @Test
    void coercesConsequenceValueToNumber() {
        Book prisoner = loadBundled("the-prisoner");

        Consequence consequence = prisoner.findSection(20).orElseThrow().options().getFirst().consequence();

        assertThat(consequence.type()).isEqualTo(ConsequenceType.LOSE_HEALTH);
        assertThat(consequence.value()).isEqualTo(6);
    }

    @Test
    void sectionWithoutOptionsHasEmptyList() {
        assertThat(loadBundled("the-prisoner").findSection(666).orElseThrow().options()).isEmpty();
    }

    @Test
    void skipsMalformedFiles() {
        List<Book> books = new BookLoader(jsonMapper, "classpath:books-fixtures/*.json").loadAll();

        assertThat(books).extracting(Book::id).containsExactly("tiny");
    }

    @Test
    void ignoresFieldsItDoesNotKnow() {
        assertThat(loadFixture("extra-fields").sections()).hasSize(2);
    }

    @Test
    void skipsFilesWithUnknownValuesOrTheWrongShape() {
        List<Book> books = new BookLoader(jsonMapper, "classpath:loader-fixtures/*.json").loadAll();

        assertThat(books).extracting(Book::id)
                .containsExactlyInAnyOrder("extra-fields", "the.second.edition")
                .doesNotContain("unknown-consequence", "not-an-object");
    }

    @Test
    void idIsTheFileNameWithoutOnlyTheJsonExtension() {
        assertThat(loadFixture("the.second.edition").title()).isEqualTo("Dots In Name");
    }

    @Test
    void missingDifficultyIsNull() {
        assertThat(loadFixture("the.second.edition").difficulty()).isNull();
    }

    @Test
    void failsFastWhenTheBooksFolderDoesNotExist() {
        BookLoader loader = new BookLoader(jsonMapper, "classpath:no-such-folder/*.json");

        assertThatThrownBy(loader::loadAll)
                .isInstanceOf(UncheckedIOException.class)
                .hasMessage("Could not list book files at classpath:no-such-folder/*.json");
    }

    @Test
    void folderWithoutMatchingFilesLoadsNothing() {
        assertThat(new BookLoader(jsonMapper, "classpath:loader-fixtures/*.yaml").loadAll()).isEmpty();
    }

    private Book loadFixture(String id) {
        return new BookLoader(jsonMapper, "classpath:loader-fixtures/*.json").loadAll().stream()
                .filter(b -> b.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private Book loadBundled(String id) {
        return new BookLoader(jsonMapper, "classpath:books/*.json").loadAll().stream()
                .filter(b -> b.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
