package com.adventurebook.repository;

import com.adventurebook.model.Game;
import com.adventurebook.model.GameStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameRepositoryTest {

    private final GameRepository repository = new GameRepository();

    private static Game game(int sectionId) {
        return new Game("g1", "book", sectionId, 10, GameStatus.IN_PROGRESS, null);
    }

    @Test
    void updateReplacesTheStoredGame() {
        repository.save(game(1));

        assertThat(repository.update("g1", game -> game(20))).hasValue(game(20));
        assertThat(repository.findById("g1")).hasValue(game(20));
    }

    @Test
    void failedUpdateLeavesTheGameUnchanged() {
        repository.save(game(1));

        assertThatThrownBy(() -> repository.update("g1", game -> {
            throw new IllegalStateException("rejected");
        })).hasMessage("rejected");
        assertThat(repository.findById("g1")).hasValue(game(1));
    }

    @Test
    void updatingAnUnknownGameFindsNothing() {
        assertThat(repository.update("missing", game -> game)).isEmpty();
    }
}
