package com.adventurebook.book;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookLoaderTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void loadsBundledBooksAndSkipsTheEmptyOne() {
        List<Book> books = new BookLoader(jsonMapper, "classpath:books/*.json").loadAll();

        assertThat(books).extracting(Book::id)
                .containsExactly("crystal-caverns", "pirates-jade-sea", "the-prisoner");
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
    void treatsBlankCategoryAsMissing() {
        assertThat(loadBundled("pirates-jade-sea").category()).isNull();
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

    private Book loadBundled(String id) {
        return new BookLoader(jsonMapper, "classpath:books/*.json").loadAll().stream()
                .filter(b -> b.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
