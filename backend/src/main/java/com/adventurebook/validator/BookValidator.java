package com.adventurebook.validator;

import com.adventurebook.model.Book;
import com.adventurebook.model.Option;
import com.adventurebook.model.Section;
import com.adventurebook.model.SectionType;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookValidator {

    public ValidationResult validate(Book book) {
        List<String> errors = new ArrayList<>();
        Map<Integer, Section> sectionsById = new HashMap<>();
        Set<Integer> duplicateIds = new LinkedHashSet<>();
        for (Section section : book.sections()) {
            if (sectionsById.putIfAbsent(section.id(), section) != null) {
                duplicateIds.add(section.id());
            }
        }
        for (int id : duplicateIds) {
            errors.add("Section " + id + " is defined more than once");
        }

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
        Deque<Section> toVisit = new ArrayDeque<>();
        for (Section start : starts) {
            if (visited.add(start.id())) {
                toVisit.push(start);
            }
        }
        while (!toVisit.isEmpty()) {
            for (Option option : toVisit.pop().options()) {
                Section next = sectionsById.get(option.gotoId());
                if (next != null && visited.add(next.id())) {
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
