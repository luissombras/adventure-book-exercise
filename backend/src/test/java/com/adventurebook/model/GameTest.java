package com.adventurebook.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.adventurebook.model.ConsequenceType.GAIN_HEALTH;
import static com.adventurebook.model.ConsequenceType.LOSE_HEALTH;
import static com.adventurebook.model.SectionType.BEGIN;
import static com.adventurebook.model.SectionType.END;
import static com.adventurebook.model.SectionType.NODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private static final Consequence SCRATCH = new Consequence(LOSE_HEALTH, 3, "A scratch.");
    private static final Consequence POTION = new Consequence(GAIN_HEALTH, 5, "A potion.");
    private static final Consequence FALL = new Consequence(LOSE_HEALTH, 20, "A long fall.");

    private final Book book = new Book("test", "Test", "Tester", Difficulty.EASY, List.of(
            new Section(1, "Start", BEGIN, List.of(
                    new Option("Walk", 2, null),
                    new Option("Scratch", 2, SCRATCH),
                    new Option("Potion", 2, POTION),
                    new Option("Jump", 2, FALL),
                    new Option("Leave", 3, SCRATCH))),
            new Section(2, "Middle", NODE, List.of(new Option("Back", 1, null))),
            new Section(3, "End", END, List.of())));

    @Test
    void startsAtTheBeginningWithFullHealth() {
        Game game = Game.start("g1", book);

        assertThat(game.sectionId()).isEqualTo(1);
        assertThat(game.health()).isEqualTo(Game.MAX_HEALTH);
        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.lastOutcome()).isNull();
    }

    @Test
    void choiceWithoutConsequenceOnlyMoves() {
        Game game = Game.start("g1", book).choose(book, 0);

        assertThat(game.sectionId()).isEqualTo(2);
        assertThat(game.health()).isEqualTo(10);
        assertThat(game.lastOutcome()).isNull();
    }

    @Test
    void losingHealthIsAppliedAndRemembered() {
        Game game = Game.start("g1", book).choose(book, 1);

        assertThat(game.health()).isEqualTo(7);
        assertThat(game.lastOutcome()).isEqualTo(new Outcome("A scratch.", -3));
    }

    @Test
    void healthNeverGoesAboveTheMaximumAndOnlyTheAppliedGainIsReported() {
        Game game = Game.start("g1", book).choose(book, 1).choose(book, 0).choose(book, 2);

        assertThat(game.health()).isEqualTo(Game.MAX_HEALTH);
        assertThat(game.lastOutcome()).isEqualTo(new Outcome("A potion.", 3));
    }

    @Test
    void gainingHealthAtFullHealthChangesNothing() {
        Game game = Game.start("g1", book).choose(book, 2);

        assertThat(game.health()).isEqualTo(Game.MAX_HEALTH);
        assertThat(game.lastOutcome()).isEqualTo(new Outcome("A potion.", 0));
    }

    @Test
    void reachingZeroHealthKillsThePlayerWhereTheyStand() {
        Game game = Game.start("g1", book).choose(book, 3);

        assertThat(game.status()).isEqualTo(GameStatus.DEAD);
        assertThat(game.health()).isZero();
        assertThat(game.sectionId()).isEqualTo(1);
        assertThat(game.lastOutcome()).isEqualTo(new Outcome("A long fall.", -10));
    }

    @Test
    void dyingOnTheWayToAnEndingIsStillDeath() {
        Game game = new Game("g1", "test", 1, 3, GameStatus.IN_PROGRESS, null).choose(book, 4);

        assertThat(game.status()).isEqualTo(GameStatus.DEAD);
        assertThat(game.sectionId()).isEqualTo(1);
    }

    @Test
    void reachingAnEndingFinishesTheGame() {
        Game game = Game.start("g1", book).choose(book, 4);

        assertThat(game.status()).isEqualTo(GameStatus.FINISHED);
        assertThat(game.sectionId()).isEqualTo(3);
        assertThat(game.health()).isEqualTo(7);
    }

    @Test
    void noChoicesOnceTheGameIsOver() {
        Game dead = Game.start("g1", book).choose(book, 3);
        Game finished = Game.start("g1", book).choose(book, 4);

        assertThatThrownBy(() -> dead.choose(book, 0)).isInstanceOf(InvalidChoiceException.class)
                .hasMessage("The adventure is already over");
        assertThatThrownBy(() -> finished.choose(book, 0)).isInstanceOf(InvalidChoiceException.class);
    }

    @Test
    void rejectsAnOptionTheSectionDoesNotHave() {
        Game game = Game.start("g1", book);

        assertThatThrownBy(() -> game.choose(book, 5)).isInstanceOf(InvalidChoiceException.class)
                .hasMessage("Section 1 has no option 5");
        assertThatThrownBy(() -> game.choose(book, -1)).isInstanceOf(InvalidChoiceException.class);
    }
}
