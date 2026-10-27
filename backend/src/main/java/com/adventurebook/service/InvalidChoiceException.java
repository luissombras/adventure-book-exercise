package com.adventurebook.service;

public class InvalidChoiceException extends RuntimeException {

    public InvalidChoiceException(String message) {
        super(message);
    }
}
