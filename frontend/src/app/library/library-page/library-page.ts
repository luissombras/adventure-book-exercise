import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { BookSummary, DIFFICULTIES, Difficulty } from '../books/book.model';
import { Icon } from '../shared/icon';
import { BookCard } from './book-card';

@Component({
  selector: 'app-library-page',
  imports: [BookCard, Icon, TitleCasePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './library-page.html',
  styleUrl: './library-page.scss',
})
export class LibraryPage {
  protected readonly difficulties = DIFFICULTIES;

  protected readonly search = signal('');
  protected readonly selectedDifficulties = signal<Difficulty[]>([]);

  private readonly library = httpResource<BookSummary[]>(() => '/api/books');

  protected readonly loading = this.library.isLoading;
  protected readonly failed = computed(() => !!this.library.error());
  protected readonly allBooks = computed(() => (this.library.hasValue() ? this.library.value() : []));

  protected readonly filtering = computed(() => this.search().trim() !== '' || this.selectedDifficulties().length > 0);

  protected readonly books = computed(() => {
    const text = this.search().trim().toLowerCase();
    const difficulties = this.selectedDifficulties();
    return this.allBooks().filter(
      (book) =>
        (!text || `${book.title} ${book.author}`.toLowerCase().includes(text)) &&
        (!difficulties.length || (book.difficulty !== null && difficulties.includes(book.difficulty))),
    );
  });

  protected onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  protected toggleDifficulty(difficulty: Difficulty): void {
    this.selectedDifficulties.update((selected) =>
      selected.includes(difficulty) ? selected.filter((d) => d !== difficulty) : [...selected, difficulty],
    );
  }

  protected clearFilters(): void {
    this.search.set('');
    this.selectedDifficulties.set([]);
  }

  protected reload(): void {
    this.library.reload();
  }
}
