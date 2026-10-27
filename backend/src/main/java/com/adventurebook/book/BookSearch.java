package com.adventurebook.book;

import java.util.List;
import java.util.Locale;

public record BookSearch(String text, List<Difficulty> difficulties, List<String> categories) {

    public BookSearch {
        text = text == null ? "" : text.strip().toLowerCase(Locale.ROOT);
        difficulties = difficulties == null ? List.of() : List.copyOf(difficulties);
        categories = categories == null ? List.of() : List.copyOf(categories);
    }

    public static BookSearch all() {
        return new BookSearch(null, null, null);
    }

    public boolean matches(Book book) {
        return matchesText(book) && matchesDifficulty(book) && matchesCategory(book);
    }

    private boolean matchesText(Book book) {
        return text.isEmpty()
                || contains(book.title())
                || contains(book.author())
                || contains(book.category());
    }

    private boolean matchesDifficulty(Book book) {
        return difficulties.isEmpty() || difficulties.contains(book.difficulty());
    }

    private boolean matchesCategory(Book book) {
        return categories.isEmpty()
                || categories.stream().anyMatch(c -> c.equalsIgnoreCase(book.category()));
    }

    private boolean contains(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(text);
    }
}
