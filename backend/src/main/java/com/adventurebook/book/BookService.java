package com.adventurebook.book;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;
    private final BookValidator validator;

    public BookService(BookRepository repository, BookValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<BookSummary> listBooks() {
        return repository.findAll().stream()
                .map(book -> BookSummary.of(book, validator.validate(book)))
                .toList();
    }
}
