export type GameStatus = 'IN_PROGRESS' | 'FINISHED' | 'DEAD';

export interface GameSection {
  id: number;
  text: string;
  options: string[];
}

export interface Consequence {
  text: string;
  healthChange: number;
}

export interface Game {
  id: string;
  bookId: string;
  bookTitle: string;
  status: GameStatus;
  health: number;
  maxHealth: number;
  consequence: Consequence | null;
  section: GameSection;
}

export interface Problem {
  detail?: string;
  errors?: string[];
}
