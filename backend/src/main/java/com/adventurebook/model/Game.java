package com.adventurebook.model;

public record Game(String id, String bookId, int sectionId, int health, GameStatus status, Consequence lastConsequence) {

    public static final int MAX_HEALTH = 10;

    public static Game start(String id, Book book) {
        Section beginning = book.beginning().orElseThrow();
        return new Game(id, book.id(), beginning.id(), MAX_HEALTH, GameStatus.IN_PROGRESS, null);
    }

    public Section currentSection(Book book) {
        return book.findSection(sectionId).orElseThrow();
    }

    public Game choose(Book book, int optionIndex) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new InvalidChoiceException("The adventure is already over");
        }
        Section current = currentSection(book);
        if (optionIndex < 0 || optionIndex >= current.options().size()) {
            throw new InvalidChoiceException("Section " + current.id() + " has no option " + optionIndex);
        }

        Option option = current.options().get(optionIndex);
        Consequence consequence = option.consequence();
        int healthChange = consequence == null ? 0 : consequence.healthChange();
        int newHealth = Math.clamp((long) health + healthChange, 0, MAX_HEALTH);
        if (newHealth == 0) {
            return new Game(id, bookId, sectionId, 0, GameStatus.DEAD, consequence);
        }

        Section next = book.findSection(option.gotoId()).orElseThrow();
        GameStatus nextStatus = next.isEnding() ? GameStatus.FINISHED : GameStatus.IN_PROGRESS;
        return new Game(id, bookId, next.id(), newHealth, nextStatus, consequence);
    }
}
