package com.adventurebook.model;

public record Game(String id, String bookId, int sectionId) {

    public static Game start(String id, Book book) {
        return new Game(id, book.id(), book.beginning().orElseThrow().id());
    }

    public Section currentSection(Book book) {
        return book.findSection(sectionId).orElseThrow();
    }

    public Game choose(Book book, int optionIndex) {
        Section current = currentSection(book);
        if (current.isEnding()) {
            throw new InvalidChoiceException("The adventure is already over");
        }
        if (optionIndex < 0 || optionIndex >= current.options().size()) {
            throw new InvalidChoiceException("Section " + current.id() + " has no option " + optionIndex);
        }
        Option option = current.options().get(optionIndex);
        return new Game(id, bookId, option.gotoId());
    }
}
