import { Routes } from '@angular/router';
import { GamePage } from './game/game-page/game-page';
import { LibraryPage } from './library/library-page/library-page';

export const routes: Routes = [
  { path: '', component: LibraryPage, title: 'Adventure Book' },
  { path: 'play/:bookId', component: GamePage, title: 'Adventure Book – Playing' },
  { path: '**', redirectTo: '' },
];
