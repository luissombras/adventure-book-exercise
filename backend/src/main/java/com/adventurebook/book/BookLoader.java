package com.adventurebook.book;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Reads the book JSON files bundled with the application.
 * A file that can't be parsed (empty, malformed...) is skipped with a warning
 * instead of preventing the application from starting.
 */
@Component
public class BookLoader {

    private static final Logger log = LoggerFactory.getLogger(BookLoader.class);

    private final JsonMapper jsonMapper;
    private final String location;
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public BookLoader(JsonMapper jsonMapper,
                      @Value("${adventure-book.books-location:classpath:books/*.json}") String location) {
        this.jsonMapper = jsonMapper;
        this.location = location;
    }

    public List<Book> loadAll() {
        List<Book> books = new ArrayList<>();
        for (Resource resource : findResources()) {
            load(resource).ifPresent(books::add);
        }
        log.info("Loaded {} book(s) from {}", books.size(), location);
        return books;
    }

    private Resource[] findResources() {
        try {
            Resource[] resources = resolver.getResources(location);
            Arrays.sort(resources, Comparator.comparing(Resource::getFilename, Comparator.nullsLast(String::compareTo)));
            return resources;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list book files at " + location, e);
        }
    }

    private Optional<Book> load(Resource resource) {
        String fileName = resource.getFilename();
        try (InputStream in = resource.getInputStream()) {
            Book book = jsonMapper.readValue(in, Book.class);
            return Optional.of(book.withId(idFromFileName(fileName)));
        } catch (IOException | JacksonException e) {
            log.warn("Skipping book file {}: {}", fileName, e.getMessage());
            return Optional.empty();
        }
    }

    static String idFromFileName(String fileName) {
        return fileName.replaceFirst("\\.json$", "");
    }
}
