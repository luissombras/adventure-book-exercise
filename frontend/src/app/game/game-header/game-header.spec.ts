import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Game } from '../game.model';
import { GameHeader } from './game-header';

const game: Game = {
  id: 'game-1',
  bookId: 'the-prisoner',
  bookTitle: 'The Prisoner',
  status: 'IN_PROGRESS',
  health: 6,
  maxHealth: 10,
  consequence: null,
  section: { id: 1, text: 'A cell.', options: ['Wait'] },
};

describe('GameHeader', () => {
  function render(value: Game | null): HTMLElement {
    TestBed.configureTestingModule({ imports: [GameHeader], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(GameHeader);
    fixture.componentRef.setInput('game', value);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('always offers the way back to the library', () => {
    const element = render(null);

    expect(element.querySelector('a')?.getAttribute('href')).toBe('/');
    expect(element.querySelector('.game-header__title')).toBeNull();
    expect(element.querySelector('app-health-meter')).toBeNull();
  });

  it('shows the book and the health of the game being played', () => {
    const element = render(game);

    expect(element.querySelector('.game-header__title')?.textContent).toContain('The Prisoner');
    expect(element.querySelector('.health__value')?.textContent).toContain('6 / 10');
  });
});
