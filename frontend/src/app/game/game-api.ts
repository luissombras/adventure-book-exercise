import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Game } from './game.model';

const GAMES_URL = '/api/games';

@Injectable({ providedIn: 'root' })
export class GameApi {
  private readonly http = inject(HttpClient);

  start(bookId: string): Observable<Game> {
    return this.http.post<Game>(GAMES_URL, { bookId });
  }

  choose(gameId: string, optionIndex: number): Observable<Game> {
    return this.http.post<Game>(`${GAMES_URL}/${gameId}/choices`, { optionIndex });
  }
}
