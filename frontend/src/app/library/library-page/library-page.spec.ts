import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { BookSummary } from '../../books/book.model';
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

  function chip(element: HTMLElement, label: string): HTMLButtonElement {
    return [...element.querySelectorAll<HTMLButtonElement>('.filters .chip')].find((c) => c.textContent?.trim() === label)!;
  }

  async function search(fixture: Awaited<ReturnType<typeof renderWith>>, text: string) {
    const input = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>('.search__input')!;
    input.value = text;
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  function titles(element: HTMLElement): string[] {
    return [...element.querySelectorAll('app-book-card h3')].map((h) => h.textContent!.trim());
  }

  it('uses the singular for a single book', async () => {
    const fixture = await renderWith([book('a')]);

    expect((fixture.nativeElement as HTMLElement).querySelector('.hero__count')?.textContent).toContain(
      '1 Epic Adventure Available',
    );
  });

  it('combines several difficulties with OR', async () => {
    const fixture = await renderWith([book('a'), book('b', { difficulty: 'MEDIUM' }), book('c', { difficulty: 'HARD' })]);
    const element = fixture.nativeElement as HTMLElement;

    chip(element, 'Easy').click();
    chip(element, 'Hard').click();
    await fixture.whenStable();

    expect(titles(element)).toEqual(['a', 'c']);
  });

  it('combines search and difficulty with AND', async () => {
    const fixture = await renderWith([
      book('Dragon Lair', { difficulty: 'HARD' }),
      book('Dragon Egg', { difficulty: 'EASY' }),
      book('Sea Song', { difficulty: 'HARD' }),
    ]);
    const element = fixture.nativeElement as HTMLElement;

    chip(element, 'Hard').click();
    await search(fixture, '  dragon ');

    expect(titles(element)).toEqual(['Dragon Lair']);
  });

  it('toggles a difficulty off when it is clicked again', async () => {
    const fixture = await renderWith([book('a'), book('b', { difficulty: 'HARD' })]);
    const element = fixture.nativeElement as HTMLElement;

    chip(element, 'Hard').click();
    await fixture.whenStable();
    chip(element, 'Hard').click();
    await fixture.whenStable();

    expect(titles(element)).toEqual(['a', 'b']);
    expect(chip(element, 'Hard').getAttribute('aria-pressed')).toBe('false');
  });

  it('clears the search and every filter at once', async () => {
    const fixture = await renderWith([book('a'), book('b', { difficulty: 'HARD' })]);
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.chip--clear')).toBeNull();
    chip(element, 'Hard').click();
    await search(fixture, 'zzz');
    expect(element.textContent).toContain('No adventures match your search.');

    element.querySelector<HTMLButtonElement>('.chip--clear')!.click();
    await fixture.whenStable();

    expect(titles(element)).toEqual(['a', 'b']);
    expect(element.querySelector<HTMLInputElement>('.search__input')!.value).toBe('');
    expect(element.querySelector('.chip--clear')).toBeNull();
  });

  it('shows a loading message until the library arrives', () => {
    const fixture = TestBed.createComponent(LibraryPage);
    TestBed.tick();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Dusting off the shelves...');
    httpMock.expectOne('/api/books').flush([]);
  });

  it('loads the library again when the user retries after an error', async () => {
    const fixture = TestBed.createComponent(LibraryPage);
    TestBed.tick();
    httpMock.expectOne('/api/books').flush('down', { status: 502, statusText: 'Bad Gateway' });
    await fixture.whenStable();
    const element = fixture.nativeElement as HTMLElement;

    element.querySelector<HTMLButtonElement>('.state--error .chip')!.click();
    TestBed.tick();
    httpMock.expectOne('/api/books').flush([book('a')]);
    await fixture.whenStable();

    expect(element.querySelector('.state--error')).toBeNull();
    expect(titles(element)).toEqual(['a']);
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
