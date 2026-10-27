import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { BookFilters, BookSummary, DIFFICULTIES, Difficulty } from '../books/book.model';
import { BOOKS_URL, booksRequest, hasActiveFilters } from '../books/books-api';
import { Icon } from '../shared/icon';
import { BookCard } from './book-card';

const SEARCH_DEBOUNCE_MS = 250;

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
  protected readonly selectedCategories = signal<string[]>([]);

  private readonly debouncedSearch = toSignal(
    toObservable(this.search).pipe(
      debounceTime(SEARCH_DEBOUNCE_MS),
      map((value) => value.trim()),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );

  private readonly filters = computed<BookFilters>(() => ({
    search: this.debouncedSearch(),
    difficulties: this.selectedDifficulties(),
    categories: this.selectedCategories(),
  }));

  protected readonly filtering = computed(() => hasActiveFilters(this.filters()));

  private readonly catalog = httpResource<BookSummary[]>(() => BOOKS_URL, { defaultValue: [] });

  private readonly filtered = httpResource<BookSummary[]>(
    () => (this.filtering() ? booksRequest(this.filters()) : undefined),
    { defaultValue: [] },
  );

  private readonly allBooks = computed(() => (this.catalog.hasValue() ? this.catalog.value() : []));
  private readonly filteredBooks = computed(() => (this.filtered.hasValue() ? this.filtered.value() : []));

  protected readonly books = computed(() => (this.filtering() ? this.filteredBooks() : this.allBooks()));
  protected readonly loading = computed(() => this.catalog.isLoading() || this.filtered.isLoading());
  protected readonly failed = computed(() => !!this.catalog.error() || !!this.filtered.error());
  protected readonly totalBooks = computed(() => this.allBooks().length);

  protected readonly categories = computed(() => {
    const categories = this.allBooks()
      .map((book) => book.category)
      .filter((category): category is string => !!category);
    return [...new Set(categories)].sort((a, b) => a.localeCompare(b));
  });

  protected onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  protected toggleDifficulty(difficulty: Difficulty): void {
    this.selectedDifficulties.update((selected) => toggle(selected, difficulty));
  }

  protected toggleCategory(category: string): void {
    this.selectedCategories.update((selected) => toggle(selected, category));
  }

  protected clearFilters(): void {
    this.search.set('');
    this.selectedDifficulties.set([]);
    this.selectedCategories.set([]);
  }

  protected reload(): void {
    this.catalog.reload();
    this.filtered.reload();
  }
}

function toggle<T>(values: T[], value: T): T[] {
  return values.includes(value) ? values.filter((v) => v !== value) : [...values, value];
}
