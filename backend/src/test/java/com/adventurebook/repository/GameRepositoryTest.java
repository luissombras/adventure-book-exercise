package com.adventurebook.repository;

import com.adventurebook.model.Game;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameRepositoryTest {

    private final GameRepository repository = new GameRepository();

    @Test
    void updateReplacesTheStoredGame() {
        repository.save(new Game("g1", "book", 1));

        assertThat(repository.update("g1", game -> new Game("g1", "book", 20))).hasValue(new Game("g1", "book", 20));
        assertThat(repository.findById("g1")).hasValue(new Game("g1", "book", 20));
    }

    @Test
    void failedUpdateLeavesTheGameUnchanged() {
        repository.save(new Game("g1", "book", 1));

        assertThatThrownBy(() -> repository.update("g1", game -> {
            throw new IllegalStateException("rejected");
        })).hasMessage("rejected");
        assertThat(repository.findById("g1")).hasValue(new Game("g1", "book", 1));
    }

    @Test
    void updatingAnUnknownGameFindsNothing() {
        assertThat(repository.update("missing", game -> game)).isEmpty();
    }
}
