package com.adventurebook.book;

/**
 * Effect applied to the player when an option is chosen.
 * The provided books store {@code value} as a string ("4"), Jackson coerces it to an int.
 */
public record Consequence(ConsequenceType type, int value, String text) {
}
