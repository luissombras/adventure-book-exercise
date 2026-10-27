package com.adventurebook.service;

import java.util.List;

public class UnplayableBookException extends RuntimeException {

    private final List<String> errors;

    public UnplayableBookException(String title, List<String> errors) {
        super(title + " can't be played");
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
