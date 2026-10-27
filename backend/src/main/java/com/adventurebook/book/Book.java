package com.adventurebook.book;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Optional;

/**
 * A book as described by the JSON files. The {@code id} is not part of the file,
 * it is assigned when the book is loaded (from the file name).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Book(
        String id,
        String title,
        String author,
        Difficulty difficulty,
        @JsonAlias("type") String category,
        List<Section> sections) {

    public Book {
        sections = sections == null ? List.of() : List.copyOf(sections);
        if (category != null && category.isBlank()) {
            category = null;
        }
    }

    public Book withId(String newId) {
        return new Book(newId, title, author, difficulty, category, sections);
    }

    public Optional<Section> findSection(int sectionId) {
        return sections.stream().filter(s -> s.id() == sectionId).findFirst();
    }
}
