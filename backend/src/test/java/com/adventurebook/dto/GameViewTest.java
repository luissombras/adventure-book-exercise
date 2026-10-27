package com.adventurebook.dto;

import com.adventurebook.model.Book;
import com.adventurebook.model.Difficulty;
import com.adventurebook.model.Game;
import com.adventurebook.model.GameStatus;
import com.adventurebook.model.Option;
import com.adventurebook.model.Outcome;
import com.adventurebook.model.Section;
import com.adventurebook.model.SectionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameViewTest {

    private final Book book = new Book("tiny", "Tiny Tale", "Tester", Difficulty.EASY, List.of(
            new Section(1, "Start", SectionType.BEGIN, List.of(new Option("Go", 2, null), new Option("Stay", 1, null))),
            new Section(2, "End", SectionType.END, List.of())));

    @Test
    void describesAGameInProgress() {
        GameView view = GameView.of(new Game("g1", "tiny", 1, 8, GameStatus.IN_PROGRESS, null), book);

        assertThat(view.id()).isEqualTo("g1");
        assertThat(view.bookId()).isEqualTo("tiny");
        assertThat(view.bookTitle()).isEqualTo("Tiny Tale");
        assertThat(view.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(view.health()).isEqualTo(8);
        assertThat(view.maxHealth()).isEqualTo(Game.MAX_HEALTH);
        assertThat(view.consequence()).isNull();
        assertThat(view.section()).isEqualTo(new GameView.SectionView(1, "Start", List.of("Go", "Stay")));
    }

    @Test
    void carriesTheLastOutcome() {
        GameView view = GameView.of(new Game("g1", "tiny", 1, 7, GameStatus.IN_PROGRESS, new Outcome("Ouch", -3)), book);

        assertThat(view.consequence()).isEqualTo(new GameView.ConsequenceView("Ouch", -3));
    }

    @Test
    void offersNoOptionsOnceTheGameIsOver() {
        GameView dead = GameView.of(new Game("g1", "tiny", 1, 0, GameStatus.DEAD, new Outcome("Fall", -10)), book);
        GameView finished = GameView.of(new Game("g1", "tiny", 2, 5, GameStatus.FINISHED, null), book);

        assertThat(dead.section().id()).isEqualTo(1);
        assertThat(dead.section().options()).isEmpty();
        assertThat(finished.section().options()).isEmpty();
    }
}
