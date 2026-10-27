package com.adventurebook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Book(String id, String title, String author, Difficulty difficulty, List<Section> sections) {

    public Book {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    public Book withId(String newId) {
        return new Book(newId, title, author, difficulty, sections);
    }

    public Optional<Section> findSection(int sectionId) {
        return sections.stream().filter(s -> s.id() == sectionId).findFirst();
    }

    public Optional<Section> beginning() {
        return sections.stream().filter(s -> s.type() == SectionType.BEGIN).findFirst();
    }
}
