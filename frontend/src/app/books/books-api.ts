import { HttpResourceRequest } from '@angular/common/http';
import { BookFilters } from './book.model';

export const BOOKS_URL = '/api/books';

export function hasActiveFilters(filters: BookFilters): boolean {
  return filters.search.trim() !== '' || filters.difficulties.length > 0 || filters.categories.length > 0;
}

/** Builds the GET /api/books request, leaving out the filters that aren't set. */
export function booksRequest(filters: BookFilters): HttpResourceRequest {
  const params: Record<string, string | string[]> = {};
  const search = filters.search.trim();
  if (search) {
    params['search'] = search;
  }
  if (filters.difficulties.length) {
    params['difficulty'] = filters.difficulties;
  }
  if (filters.categories.length) {
    params['category'] = filters.categories;
  }
  return { url: BOOKS_URL, params };
}
