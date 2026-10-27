export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export const DIFFICULTIES: readonly Difficulty[] = ['EASY', 'MEDIUM', 'HARD'];

export interface BookSummary {
  id: string;
  title: string;
  author: string;
  description: string | null;
  difficulty: Difficulty | null;
  sectionCount: number;
  valid: boolean;
  errors: string[];
}
