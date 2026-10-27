package com.adventurebook.book;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookRepository {

    private final Map<String, Book> books = new ConcurrentHashMap<>();

    public BookRepository(BookLoader loader) {
        loader.loadAll().forEach(this::save);
    }

    public List<Book> findAll() {
        return books.values().stream()
                .sorted(Comparator.comparing(Book::title, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<Book> findById(String id) {
        return Optional.ofNullable(books.get(id));
    }

    public Book save(Book book) {
        books.put(book.id(), book);
        return book;
    }
}
