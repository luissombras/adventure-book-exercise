package com.adventurebook.book;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Checks the rules a book must follow to be playable:
 * <ul>
 *     <li>exactly one beginning section</li>
 *     <li>at least one ending section</li>
 *     <li>every option leads to an existing section</li>
 *     <li>every non-ending section the player can reach has at least one option</li>
 * </ul>
 * The last rule only looks at reachable sections: a dead section nobody can get to
 * (like section 666 in the provided books) can't block a game, so it isn't an error.
 */
@Component
public class BookValidator {

    public ValidationResult validate(Book book) {
        List<String> errors = new ArrayList<>();
        Map<Integer, Section> sectionsById = book.sections().stream()
                .collect(Collectors.toMap(Section::id, Function.identity(), (first, duplicate) -> first));

        List<Section> beginnings = book.sections().stream()
                .filter(s -> s.type() == SectionType.BEGIN)
                .toList();
        if (beginnings.isEmpty()) {
            errors.add("Book has no beginning section");
        } else if (beginnings.size() > 1) {
            errors.add("Book has more than one beginning section (sections " + ids(beginnings) + ")");
        }

        if (book.sections().stream().noneMatch(Section::isEnding)) {
            errors.add("Book has no ending section");
        }

        for (Section section : book.sections()) {
            for (Option option : section.options()) {
                if (!sectionsById.containsKey(option.gotoId())) {
                    errors.add("Section " + section.id() + " has an option leading to unknown section " + option.gotoId());
                }
            }
        }

        Set<Integer> reachable = reachableFrom(beginnings, sectionsById);
        for (Section section : book.sections()) {
            if (reachable.contains(section.id()) && !section.isEnding() && section.options().isEmpty()) {
                errors.add("Section " + section.id() + " is not an ending but has no options");
            }
        }

        return new ValidationResult(errors);
    }

    private Set<Integer> reachableFrom(List<Section> starts, Map<Integer, Section> sectionsById) {
        Set<Integer> visited = new HashSet<>();
        Deque<Section> toVisit = new ArrayDeque<>(starts);
        while (!toVisit.isEmpty()) {
            Section current = toVisit.pop();
            if (!visited.add(current.id())) {
                continue;
            }
            for (Option option : current.options()) {
                Section next = sectionsById.get(option.gotoId());
                if (next != null) {
                    toVisit.push(next);
                }
            }
        }
        return visited;
    }

    private static String ids(List<Section> sections) {
        return sections.stream().map(s -> String.valueOf(s.id())).collect(Collectors.joining(", "));
    }
}
