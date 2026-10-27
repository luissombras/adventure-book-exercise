package com.adventurebook.model;

public class InvalidChoiceException extends RuntimeException {

    public InvalidChoiceException(String message) {
        super(message);
    }
}
