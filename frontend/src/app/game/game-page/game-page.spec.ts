import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Title } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { Game } from '../game.model';
import { GamePage } from './game-page';

function game(overrides: Partial<Game> = {}): Game {
  return {
    id: 'game-1',
    bookId: 'the-prisoner',
    bookTitle: 'The Prisoner',
    status: 'IN_PROGRESS',
    section: { id: 1, text: 'You wake up in a dark prison cell.', options: ['Open the door', 'Look under the bed'] },
    ...overrides,
  };
}

describe('GamePage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [GamePage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  async function startWith(response: Game) {
    const fixture = TestBed.createComponent(GamePage);
    fixture.componentRef.setInput('bookId', 'the-prisoner');
    fixture.detectChanges();
    const request = httpMock.expectOne({ method: 'POST', url: '/api/games' });
    expect(request.request.body).toEqual({ bookId: 'the-prisoner' });
    request.flush(response);
    await fixture.whenStable();
    return { fixture, element: fixture.nativeElement as HTMLElement };
  }

  it('starts a game for the book in the route and shows the first section', async () => {
    const { element } = await startWith(game());

    expect(element.querySelector('.game-header__title')?.textContent).toContain('The Prisoner');
    expect(element.querySelector('.story__text')?.textContent).toContain('You wake up in a dark prison cell.');
    expect(element.querySelectorAll('.choice').length).toBe(2);
  });

  it('names the tab after the book', async () => {
    await startWith(game());

    expect(TestBed.inject(Title).getTitle()).toBe('The Prisoner – Adventure Book');
  });

  it('starts a new game when the route switches to another book', async () => {
    const { fixture, element } = await startWith(game());

    fixture.componentRef.setInput('bookId', 'another-book');
    fixture.detectChanges();
    const request = httpMock.expectOne({ method: 'POST', url: '/api/games' });
    expect(request.request.body).toEqual({ bookId: 'another-book' });
    expect(element.querySelector('app-story-section')).toBeNull();

    request.flush(game({ id: 'game-2', bookId: 'another-book', bookTitle: 'Another Book' }));
    await fixture.whenStable();

    expect(element.querySelector('.game-header__title')?.textContent).toContain('Another Book');
  });

  it('sends the chosen option and shows the next section', async () => {
    const { fixture, element } = await startWith(game());

    element.querySelectorAll<HTMLButtonElement>('.choice')[1].click();
    const request = httpMock.expectOne({ method: 'POST', url: '/api/games/game-1/choices' });
    expect(request.request.body).toEqual({ optionIndex: 1 });
    request.flush(game({ section: { id: 20, text: "It's too dark.", options: ['Scan the area'] } }));
    await fixture.whenStable();

    expect(element.querySelector('.story__title')?.textContent).toContain('Section 20');
    expect(element.querySelector('.ending')).toBeNull();
  });

  it('shows the ending and can start again', async () => {
    const { fixture, element } = await startWith(
      game({ status: 'FINISHED', section: { id: 1000, text: 'You are now free.', options: [] } }),
    );

    expect(element.querySelector('.ending__title')?.textContent).toContain('The End');

    element.querySelector<HTMLButtonElement>('.ending .quest-button')!.click();
    httpMock.expectOne({ method: 'POST', url: '/api/games' }).flush(game());
    await fixture.whenStable();

    expect(element.querySelector('.ending')).toBeNull();
    expect(element.querySelector('.story__title')?.textContent).toContain('Section 1');
  });

  it('explains why a book cannot be played', async () => {
    const fixture = TestBed.createComponent(GamePage);
    fixture.componentRef.setInput('bookId', 'crystal-caverns');
    fixture.detectChanges();
    httpMock.expectOne('/api/games').flush(
      { detail: "The Crystal Caverns can't be played", errors: ['Section 666 is not an ending but has no options'] },
      { status: 422, statusText: 'Unprocessable Content' },
    );
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.notice__detail')?.textContent).toContain("The Crystal Caverns can't be played");
    expect(element.querySelector('.notice li')?.textContent).toContain('Section 666');
    expect(element.querySelector('app-story-section')).toBeNull();
  });

  it('shows a fallback message when the backend is unreachable', async () => {
    const fixture = TestBed.createComponent(GamePage);
    fixture.componentRef.setInput('bookId', 'the-prisoner');
    fixture.detectChanges();
    httpMock.expectOne('/api/games').flush('down', { status: 502, statusText: 'Bad Gateway' });
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain("We couldn't reach the story");
  });
});
