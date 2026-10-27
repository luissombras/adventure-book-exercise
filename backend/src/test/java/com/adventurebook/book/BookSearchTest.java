package com.adventurebook.book;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookSearchTest {

    private final Book pirates = book("Pirates of the Jade Sea", "Marina Blackwood", Difficulty.MEDIUM, "Adventure");
    private final Book prisoner = book("The Prisoner", "Daniel El Fuego", Difficulty.HARD, null);

    @Test
    void emptySearchMatchesEverything() {
        assertThat(BookSearch.all().matches(pirates)).isTrue();
        assertThat(BookSearch.all().matches(prisoner)).isTrue();
    }

    @Test
    void textMatchesTitleIgnoringCase() {
        BookSearch search = new BookSearch("  JADE ", null, null);

        assertThat(search.matches(pirates)).isTrue();
        assertThat(search.matches(prisoner)).isFalse();
    }

    @Test
    void textMatchesAuthorAndCategory() {
        assertThat(new BookSearch("fuego", null, null).matches(prisoner)).isTrue();
        assertThat(new BookSearch("advent", null, null).matches(pirates)).isTrue();
    }

    @Test
    void difficultiesAreCombinedWithOr() {
        BookSearch search = new BookSearch(null, List.of(Difficulty.EASY, Difficulty.HARD), null);

        assertThat(search.matches(prisoner)).isTrue();
        assertThat(search.matches(pirates)).isFalse();
    }

    @Test
    void categoryIgnoresCaseAndExcludesBooksWithoutCategory() {
        BookSearch search = new BookSearch(null, null, List.of("adventure"));

        assertThat(search.matches(pirates)).isTrue();
        assertThat(search.matches(prisoner)).isFalse();
    }

    @Test
    void differentFiltersAreCombinedWithAnd() {
        BookSearch search = new BookSearch("pirates", List.of(Difficulty.HARD), null);

        assertThat(search.matches(pirates)).isFalse();
    }

    private static Book book(String title, String author, Difficulty difficulty, String category) {
        return new Book(title.toLowerCase(), title, author, null, difficulty, category, List.of());
    }
}
