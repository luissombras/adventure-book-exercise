package com.adventurebook.book;

import java.util.List;

public record Section(int id, String text, SectionType type, List<Option> options) {

    public Section {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public boolean isEnding() {
        return type == SectionType.END;
    }
}
