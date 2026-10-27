import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { BookSummary } from '../../books/book.model';
import { BookCard } from './book-card';

const prisoner: BookSummary = {
  id: 'the-prisoner',
  title: 'The Prisoner',
  author: 'Daniel El Fuego',
  description: 'You wake up in what seems to be a dark prison cell.',
  difficulty: 'HARD',
  sectionCount: 6,
  valid: true,
  errors: [],
};

describe('BookCard', () => {
  function render(book: BookSummary): HTMLElement {
    TestBed.configureTestingModule({ imports: [BookCard], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(BookCard);
    fixture.componentRef.setInput('book', book);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows the book details and links to the game', () => {
    const element = render(prisoner);

    expect(element.querySelector('h3')?.textContent).toContain('The Prisoner');
    expect(element.textContent).toContain('by Daniel El Fuego');
    expect(element.querySelector('.badge--hard')?.textContent).toContain('Hard');
    expect(element.textContent).toContain('6 sections');
    expect(element.querySelector('a.quest-button')?.getAttribute('href')).toBe('/play/the-prisoner');
  });

  it('explains why an invalid book cannot be played', () => {
    const element = render({ ...prisoner, valid: false, errors: ['Book has no ending section'] });

    expect(element.querySelector('.badge--invalid')).not.toBeNull();
    expect(element.querySelector('.card__errors li')?.textContent).toContain('Book has no ending section');
    expect(element.querySelector('a.quest-button')).toBeNull();
    expect(element.querySelector<HTMLButtonElement>('button.quest-button')?.disabled).toBe(true);
  });
});
