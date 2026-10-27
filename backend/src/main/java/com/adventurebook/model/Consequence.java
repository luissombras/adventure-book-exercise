package com.adventurebook.model;

public record Consequence(ConsequenceType type, int value, String text) {

    public int healthChange() {
        if (type == null) {
            return 0;
        }
        return switch (type) {
            case GAIN_HEALTH -> value;
            case LOSE_HEALTH -> -value;
        };
    }
}
