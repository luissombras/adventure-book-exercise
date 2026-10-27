import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { BookSummary } from '../books/book.model';
import { LibraryPage } from './library-page';

function book(id: string, overrides: Partial<BookSummary> = {}): BookSummary {
  return {
    id,
    title: id,
    author: 'Someone',
    description: null,
    difficulty: 'EASY',
    sectionCount: 3,
    valid: true,
    errors: [],
    ...overrides,
  };
}

describe('LibraryPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LibraryPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  async function renderWith(books: BookSummary[]) {
    const fixture = TestBed.createComponent(LibraryPage);
    TestBed.tick();
    httpMock.expectOne('/api/books').flush(books);
    await fixture.whenStable();
    return fixture;
  }

  it('lists every book and counts them in the hero', async () => {
    const fixture = await renderWith([book('a'), book('b')]);
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelectorAll('app-book-card').length).toBe(2);
    expect(element.querySelector('.hero__count')?.textContent).toContain('2 Epic Adventures Available');
  });

  it('filters by difficulty without asking the API again', async () => {
    const fixture = await renderWith([book('a'), book('b', { difficulty: 'HARD' })]);
    const element = fixture.nativeElement as HTMLElement;

    const hardChip = [...element.querySelectorAll<HTMLButtonElement>('.filters .chip')].find((c) => c.textContent?.trim() === 'Hard');
    hardChip!.click();
    await fixture.whenStable();

    expect(element.querySelectorAll('app-book-card').length).toBe(1);
    expect(hardChip!.getAttribute('aria-pressed')).toBe('true');
  });

  it('searches titles and authors ignoring case', async () => {
    const fixture = await renderWith([book('The Prisoner'), book('Pirates', { author: 'Marina Blackwood' })]);
    const element = fixture.nativeElement as HTMLElement;
    const input = element.querySelector<HTMLInputElement>('.search__input')!;

    input.value = 'BLACKWOOD';
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    expect(element.querySelectorAll('app-book-card').length).toBe(1);
    expect(element.querySelector('app-book-card h3')?.textContent).toContain('Pirates');
  });

  it('tells the user when nothing matches', async () => {
    const fixture = await renderWith([]);

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No adventures match your search.');
  });

  it('shows an error when the library cannot be loaded', async () => {
    const fixture = TestBed.createComponent(LibraryPage);
    TestBed.tick();
    httpMock.expectOne('/api/books').flush('down', { status: 502, statusText: 'Bad Gateway' });
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain("We couldn't reach the library");
  });
});
