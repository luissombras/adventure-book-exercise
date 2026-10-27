package com.adventurebook.repository;

import com.adventurebook.model.Book;
import com.adventurebook.model.Difficulty;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookRepositoryTest {

    private final BookRepository repository =
            new BookRepository(new BookLoader(JsonMapper.builder().build(), "classpath:books-fixtures/*.json"));

    @Test
    void loadsTheBooksFromTheLoaderOnCreation() {
        assertThat(repository.findAll()).extracting(Book::id).containsExactly("tiny");
    }

    @Test
    void listsBooksByTitleIgnoringCase() {
        repository.save(book("b", "beta"));
        repository.save(book("a", "Alpha"));
        repository.save(book("c", "Gamma"));

        assertThat(repository.findAll()).extracting(Book::title).containsExactly("Alpha", "beta", "Gamma", "Tiny");
    }

    @Test
    void savingABookWithAnExistingIdReplacesIt() {
        repository.save(book("tiny", "Renamed"));

        assertThat(repository.findAll()).extracting(Book::title).containsExactly("Renamed");
        assertThat(repository.findById("tiny")).hasValueSatisfying(b -> assertThat(b.title()).isEqualTo("Renamed"));
    }

    @Test
    void unknownIdFindsNothing() {
        assertThat(repository.findById("missing")).isEmpty();
    }

    private static Book book(String id, String title) {
        return new Book(id, title, "Author", Difficulty.EASY, List.of());
    }
}
