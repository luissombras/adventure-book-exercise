package com.adventurebook.model;

import com.adventurebook.repository.BookLoader;
import com.adventurebook.validator.BookValidator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GamePropertiesTest {

    private static final int BOOKS = 300;
    private static final int GAMES_PER_BOOK = 20;
    private static final int MAX_STEPS = 200;

    private final BookValidator validator = new BookValidator();

    @Test
    void randomGamesOnRandomValidBooksKeepEveryRule() {
        Random random = new Random(20261027);
        for (int b = 0; b < BOOKS; b++) {
            Book book = randomValidBook(random, "book-" + b);
            assertThat(validator.validate(book).errors()).as("generated book %s", book.id()).isEmpty();
            for (int g = 0; g < GAMES_PER_BOOK; g++) {
                playRandomly(book, random);
            }
        }
    }

    @Test
    void randomGamesOnThePrisonerKeepEveryRule() {
        Book prisoner = new BookLoader(JsonMapper.builder().build(), "classpath:books/*.json").loadAll().stream()
                .filter(book -> book.id().equals("the-prisoner"))
                .findFirst()
                .orElseThrow();
        Random random = new Random(7);
        for (int g = 0; g < 500; g++) {
            playRandomly(prisoner, random);
        }
    }

    private void playRandomly(Book book, Random random) {
        Game game = Game.start("g", book);
        assertInvariants(game, book);

        for (int step = 0; step < MAX_STEPS && game.status() == GameStatus.IN_PROGRESS; step++) {
            Section current = game.currentSection(book);
            int index = random.nextInt(current.options().size());
            Option option = current.options().get(index);

            Game next = game.choose(book, index);

            assertInvariants(next, book);
            if (option.consequence() == null) {
                assertThat(next.lastOutcome()).isNull();
                assertThat(next.health()).isEqualTo(game.health());
            } else {
                assertThat(next.lastOutcome().text()).isEqualTo(option.consequence().text());
                assertThat(next.lastOutcome().healthChange()).isEqualTo(next.health() - game.health());
            }
            if (next.status() == GameStatus.DEAD) {
                assertThat(next.sectionId()).isEqualTo(game.sectionId());
            } else {
                assertThat(next.sectionId()).isEqualTo(option.gotoId());
            }
            game = next;
        }

        if (game.status() != GameStatus.IN_PROGRESS) {
            Game over = game;
            assertThatThrownBy(() -> over.choose(book, 0)).isInstanceOf(InvalidChoiceException.class);
        }
    }

    private static void assertInvariants(Game game, Book book) {
        Section section = game.currentSection(book);
        assertThat(game.health()).isBetween(0, Game.MAX_HEALTH);
        assertThat(game.status() == GameStatus.DEAD).isEqualTo(game.health() == 0);
        if (game.status() == GameStatus.FINISHED) {
            assertThat(section.isEnding()).isTrue();
        }
        if (game.status() == GameStatus.IN_PROGRESS) {
            assertThat(section.isEnding()).isFalse();
            assertThat(section.options()).isNotEmpty();
        }
    }

    private static Book randomValidBook(Random random, String id) {
        int size = 2 + random.nextInt(11);
        List<Integer> ids = random.ints(1, 1000).distinct().limit(size).boxed().toList();
        int endings = 1 + random.nextInt(Math.max(1, size / 3));

        List<Section> sections = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            SectionType type = i == 0 ? SectionType.BEGIN : i <= endings ? SectionType.END : SectionType.NODE;
            List<Option> options = type == SectionType.END
                    ? List.of()
                    : IntStream.range(0, 1 + random.nextInt(3))
                            .mapToObj(o -> new Option("Option " + o, ids.get(random.nextInt(size)), randomConsequence(random)))
                            .toList();
            sections.add(new Section(ids.get(i), "Section " + ids.get(i), type, options));
        }
        return new Book(id, id, "Generator", Difficulty.MEDIUM, sections);
    }

    private static Consequence randomConsequence(Random random) {
        return switch (random.nextInt(4)) {
            case 0 -> new Consequence(ConsequenceType.LOSE_HEALTH, 1 + random.nextInt(8), "Hurt");
            case 1 -> new Consequence(ConsequenceType.GAIN_HEALTH, 1 + random.nextInt(5), "Healed");
            default -> null;
        };
    }
}
