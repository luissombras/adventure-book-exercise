package com.adventurebook.service;

import com.adventurebook.dto.BookSummary;
import com.adventurebook.model.Book;
import com.adventurebook.repository.BookLoader;
import com.adventurebook.repository.BookRepository;
import com.adventurebook.validator.BookValidator;
import com.adventurebook.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class BookServiceTest {

    private final BookRepository repository =
            new BookRepository(new BookLoader(JsonMapper.builder().build(), "classpath:books-fixtures/*.json"));
    private final AtomicInteger validations = new AtomicInteger();
    private final BookValidator validator = new BookValidator() {
        @Override
        public ValidationResult validate(Book book) {
            validations.incrementAndGet();
            return super.validate(book);
        }
    };
    private final BookService service = new BookService(repository, validator);

    @Test
    void validatesEachBookOnlyOnce() {
        service.listBooks();
        service.listBooks();

        assertThat(validations).hasValue(1);
    }

    @Test
    void summariesCarryTheValidationResult() {
        assertThat(service.listBooks()).singleElement()
                .extracting(BookSummary::id, BookSummary::valid)
                .containsExactly("tiny", true);
    }
}
