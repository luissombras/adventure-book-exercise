export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export const DIFFICULTIES: readonly Difficulty[] = ['EASY', 'MEDIUM', 'HARD'];

export interface BookSummary {
  id: string;
  title: string;
  author: string;
  description: string | null;
  difficulty: Difficulty | null;
  category: string | null;
  sectionCount: number;
  valid: boolean;
  errors: string[];
}

export interface BookFilters {
  search: string;
  difficulties: Difficulty[];
  categories: string[];
}
