import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Observable, finalize } from 'rxjs';
import { GameApi } from '../game-api';
import { Game, Problem } from '../game.model';
import { GameHeader } from '../game-header/game-header';
import { StorySection } from '../story-section/story-section';

@Component({
  selector: 'app-game-page',
  imports: [GameHeader, RouterLink, StorySection],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-page.html',
  styleUrl: './game-page.scss',
})
export class GamePage implements OnInit {
  readonly bookId = input.required<string>();

  private readonly api = inject(GameApi);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly game = signal<Game | null>(null);
  protected readonly problem = signal<Problem | null>(null);
  protected readonly busy = signal(false);

  ngOnInit(): void {
    this.start();
  }

  protected start(): void {
    this.run(this.api.start(this.bookId()));
  }

  protected choose(optionIndex: number): void {
    const game = this.game();
    if (game && !this.busy()) {
      this.run(this.api.choose(game.id, optionIndex));
    }
  }

  private run(request: Observable<Game>): void {
    this.busy.set(true);
    this.problem.set(null);
    request
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
