package com.adventurebook.service;

import com.adventurebook.dto.BookSummary;
import com.adventurebook.model.Book;
import com.adventurebook.repository.BookRepository;
import com.adventurebook.validator.BookValidator;
import com.adventurebook.validator.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookService {

    private final BookRepository repository;
    private final BookValidator validator;
    private final Map<String, ValidationResult> validations = new ConcurrentHashMap<>();

    public BookService(BookRepository repository, BookValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<BookSummary> listBooks() {
        return repository.findAll().stream()
                .map(book -> BookSummary.of(book, validationOf(book)))
                .toList();
    }

    private ValidationResult validationOf(Book book) {
        return validations.computeIfAbsent(book.id(), id -> validator.validate(book));
    }
}
