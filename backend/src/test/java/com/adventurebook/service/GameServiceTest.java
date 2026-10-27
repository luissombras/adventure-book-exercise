package com.adventurebook.service;

import com.adventurebook.dto.GameView;
import com.adventurebook.model.GameStatus;
import com.adventurebook.model.InvalidChoiceException;
import com.adventurebook.repository.BookLoader;
import com.adventurebook.repository.BookRepository;
import com.adventurebook.repository.GameRepository;
import com.adventurebook.validator.BookValidator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceTest {

    private final BookService bookService = new BookService(
            new BookRepository(new BookLoader(JsonMapper.builder().build(), "classpath:books/*.json")),
            new BookValidator());
    private final GameService gameService = new GameService(bookService, new GameRepository());

    @Test
    void startsAtTheBeginningSection() {
        GameView game = gameService.start("the-prisoner");

        assertThat(game.bookTitle()).isEqualTo("The Prisoner");
        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.section().id()).isEqualTo(1);
        assertThat(game.section().options()).containsExactly("You try to open the door", "You look under the bed");
        assertThat(game.health()).isEqualTo(10);
        assertThat(game.maxHealth()).isEqualTo(10);
        assertThat(game.consequence()).isNull();
    }

    @Test
    void choosingAnOptionMovesToItsSection() {
        String gameId = gameService.start("the-prisoner").id();

        GameView game = gameService.choose(gameId, 1);

        assertThat(game.section().id()).isEqualTo(20);
        assertThat(gameService.find(gameId).section().id()).isEqualTo(20);
    }

    @Test
    void reachingAnEndingFinishesTheGame() {
        String gameId = gameService.start("the-prisoner").id();

        gameService.choose(gameId, 1);
        gameService.choose(gameId, 0);
        GameView game = gameService.choose(gameId, 0);

        assertThat(game.section().id()).isEqualTo(1000);
        assertThat(game.status()).isEqualTo(GameStatus.FINISHED);
        assertThat(game.section().options()).isEmpty();
        assertThat(game.health()).isEqualTo(4);
    }

    @Test
    void consequencesChangeHealthAndAreReported() {
        String gameId = gameService.start("the-prisoner").id();
        gameService.choose(gameId, 1);

        GameView game = gameService.choose(gameId, 0);

        assertThat(game.health()).isEqualTo(4);
        assertThat(game.consequence().healthChange()).isEqualTo(-6);
        assertThat(game.consequence().text()).contains("rusty nail");
    }

    @Test
    void tryingTheLockedDoorTooOftenKillsThePlayer() {
        String gameId = gameService.start("the-prisoner").id();

        GameView game = null;
        for (int attempt = 0; attempt < 4; attempt++) {
            gameService.choose(gameId, 0);
            game = gameService.choose(gameId, 0);
        }

        assertThat(game.status()).isEqualTo(GameStatus.DEAD);
        assertThat(game.health()).isZero();
        assertThat(game.section().id()).isEqualTo(500);
        assertThat(game.section().options()).isEmpty();
        assertThatThrownBy(() -> gameService.choose(gameId, 0)).isInstanceOf(InvalidChoiceException.class);
    }

    @Test
    void cannotChooseOnceTheGameIsFinished() {
        String gameId = gameService.start("the-prisoner").id();
        gameService.choose(gameId, 1);
        gameService.choose(gameId, 0);
        gameService.choose(gameId, 0);

        assertThatThrownBy(() -> gameService.choose(gameId, 0))
                .isInstanceOf(InvalidChoiceException.class)
                .hasMessage("The adventure is already over");
    }

    @Test
    void rejectsAnOptionTheSectionDoesNotHave() {
        String gameId = gameService.start("the-prisoner").id();

        assertThatThrownBy(() -> gameService.choose(gameId, 2))
                .isInstanceOf(InvalidChoiceException.class)
                .hasMessage("Section 1 has no option 2");
        assertThatThrownBy(() -> gameService.choose(gameId, -1))
                .isInstanceOf(InvalidChoiceException.class);
    }

    @Test
    void invalidBooksCannotBeStarted() {
        assertThatThrownBy(() -> gameService.start("crystal-caverns"))
                .isInstanceOfSatisfying(UnplayableBookException.class, e -> {
                    assertThat(e).hasMessage("The Crystal Caverns can't be played");
                    assertThat(e.errors()).containsExactly("Section 666 is not an ending but has no options");
                });
    }

    @Test
    void unknownBooksAndGamesAreNotFound() {
        assertThatThrownBy(() -> gameService.start("missing")).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> gameService.find("missing")).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> gameService.choose("missing", 0)).isInstanceOf(NotFoundException.class);
    }
}
