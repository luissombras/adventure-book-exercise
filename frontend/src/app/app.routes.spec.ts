import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { appConfig } from './app.config';
import { GamePage } from './game/game-page/game-page';
import { LibraryPage } from './library/library-page/library-page';

describe('routes', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [...appConfig.providers, provideHttpClientTesting()] });
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('shows the library at the root', async () => {
    const harness = await RouterTestingHarness.create();

    const page = await harness.navigateByUrl('/', LibraryPage);

    expect(page).toBeInstanceOf(LibraryPage);
    httpMock.expectOne('/api/books');
  });

  it('binds the book id from the url to the game page', async () => {
    const harness = await RouterTestingHarness.create();

    await harness.navigateByUrl('/play/the-prisoner', GamePage);

    expect(httpMock.expectOne('/api/games').request.body).toEqual({ bookId: 'the-prisoner' });
  });

  it('sends unknown urls back to the library', async () => {
    const harness = await RouterTestingHarness.create();

    await harness.navigateByUrl('/no/such/page');

    expect(TestBed.inject(Router).url).toBe('/');
    httpMock.expectOne('/api/books');
  });
});
