package com.adventurebook.model;

public record Game(String id, String bookId, int sectionId) {

    public Game moveTo(int nextSectionId) {
        return new Game(id, bookId, nextSectionId);
    }
}
