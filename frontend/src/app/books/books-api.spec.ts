import { booksRequest, hasActiveFilters } from './books-api';

describe('booksRequest', () => {
  it('sends no params when nothing is filtered', () => {
    expect(booksRequest({ search: '  ', difficulties: [], categories: [] })).toEqual({
      url: '/api/books',
      params: {},
    });
  });

  it('sends trimmed search and repeated filters', () => {
    const request = booksRequest({ search: ' jade ', difficulties: ['EASY', 'HARD'], categories: ['Fantasy'] });

    expect(request.params).toEqual({ search: 'jade', difficulty: ['EASY', 'HARD'], category: ['Fantasy'] });
  });
});

describe('hasActiveFilters', () => {
  it('ignores a blank search', () => {
    expect(hasActiveFilters({ search: ' ', difficulties: [], categories: [] })).toBe(false);
  });

  it('is true as soon as one filter is set', () => {
    expect(hasActiveFilters({ search: '', difficulties: ['MEDIUM'], categories: [] })).toBe(true);
  });
});
