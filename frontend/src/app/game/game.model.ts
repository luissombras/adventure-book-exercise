export type GameStatus = 'IN_PROGRESS' | 'FINISHED';

export interface GameSection {
  id: number;
  text: string;
  options: string[];
}

export interface Game {
  id: string;
  bookId: string;
  bookTitle: string;
  status: GameStatus;
  section: GameSection;
}

export interface Problem {
  detail?: string;
  errors?: string[];
}
