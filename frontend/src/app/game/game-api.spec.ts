import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { GameApi } from './game-api';

describe('GameApi', () => {
  let api: GameApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    api = TestBed.inject(GameApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('starts a game by posting the book id', () => {
    api.start('the-prisoner').subscribe();

    const request = httpMock.expectOne({ method: 'POST', url: '/api/games' });
    expect(request.request.body).toEqual({ bookId: 'the-prisoner' });
  });

  it('makes a choice by posting the option index for that game', () => {
    api.choose('game-1', 0).subscribe();

    const request = httpMock.expectOne({ method: 'POST', url: '/api/games/game-1/choices' });
    expect(request.request.body).toEqual({ optionIndex: 0 });
  });
});
