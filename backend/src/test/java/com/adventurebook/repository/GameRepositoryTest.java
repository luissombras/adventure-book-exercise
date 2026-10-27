package com.adventurebook.repository;

import com.adventurebook.model.Game;
import com.adventurebook.model.GameStatus;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    void concurrentUpdatesOfTheSameGameAreNeverLost() throws InterruptedException {
        repository.save(new Game("g1", "book", 1, 0, GameStatus.IN_PROGRESS, null));
        int threads = 8;
        int updatesPerThread = 1_000;
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int t = 0; t < threads; t++) {
                executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < updatesPerThread; i++) {
                        repository.update("g1", game -> new Game(game.id(), game.bookId(), game.sectionId(),
                                game.health() + 1, game.status(), null));
                    }
                    return null;
                });
            }
            start.countDown();
        }

        assertThat(repository.findById("g1")).hasValueSatisfying(
                game -> assertThat(game.health()).isEqualTo(threads * updatesPerThread));
    }

    @Test
    void updatingAnUnknownGameFindsNothing() {
        assertThat(repository.update("missing", game -> game)).isEmpty();
    }
}
