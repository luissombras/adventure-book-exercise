import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject, input, signal, untracked } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Observable, Subscription, finalize } from 'rxjs';
import { ConsequenceNote } from '../consequence-note/consequence-note';
import { GameApi } from '../game-api';
import { Game, Problem } from '../game.model';
import { GameHeader } from '../game-header/game-header';
import { StorySection } from '../story-section/story-section';

@Component({
  selector: 'app-game-page',
  imports: [ConsequenceNote, GameHeader, RouterLink, StorySection],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-page.html',
  styleUrl: './game-page.scss',
})
export class GamePage {
  readonly bookId = input.required<string>();

  private readonly api = inject(GameApi);
  private readonly title = inject(Title);
  private readonly destroyRef = inject(DestroyRef);
  private pending?: Subscription;

  protected readonly game = signal<Game | null>(null);
  protected readonly problem = signal<Problem | null>(null);
  protected readonly busy = signal(false);

  constructor() {
    effect(() => {
      const bookId = this.bookId();
      untracked(() => {
        this.game.set(null);
        this.run(this.api.start(bookId));
      });
    });

    effect(() => {
      const game = this.game();
      if (game) {
        this.title.setTitle(`${game.bookTitle} – Adventure Book`);
      }
    });
  }

  protected restart(): void {
    this.run(this.api.start(this.bookId()));
  }

  protected choose(optionIndex: number): void {
    const game = this.game();
    if (game && !this.busy()) {
      this.run(this.api.choose(game.id, optionIndex));
    }
  }

  private run(request: Observable<Game>): void {
    this.pending?.unsubscribe();
    this.busy.set(true);
    this.problem.set(null);
    this.pending = request
      .pipe(
        finalize(() => this.busy.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (game) => this.game.set(game),
        error: (error: HttpErrorResponse) => this.problem.set(toProblem(error)),
      });
  }
}

function toProblem(error: HttpErrorResponse): Problem {
  const body = error.error as Problem | null;
  return body?.detail ? body : { detail: "We couldn't reach the story. Is the backend running?" };
}
