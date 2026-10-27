import { Routes } from '@angular/router';
import { LibraryPage } from './library/library-page/library-page';

export const routes: Routes = [
  { path: '', component: LibraryPage, title: 'Adventure Book' },
  { path: '**', redirectTo: '' },
];
