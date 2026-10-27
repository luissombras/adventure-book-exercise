# Adventure Book

Interactive adventure book: pick a book, read a section, make a choice, survive (or not).

- `backend/` – Spring Boot 4 (Java 25) REST API
- `frontend/` – Angular 22 app

## Running locally

Backend (port 8080):

```bash
cd backend && ./mvnw spring-boot:run
```

Frontend (port 4200, proxies `/api` to the backend):

```bash
cd frontend && npm install && npm start
```

Then open http://localhost:4200.

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npm test
```

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/books?search=&difficulty=&category=` | Library, with each book's validation errors. `difficulty` and `category` can be repeated. |

## Decisions

**Book validation.** A book is invalid when it has no beginning or more than one, has no ending, has an option
leading to a section that doesn't exist, or has a non-ending section without options. For that last rule only
sections reachable from the beginning are considered: a dead section nobody can get to can't block a game.

**Invalid books stay in the library**, marked as unplayable with the reasons, instead of being hidden.

With these rules, the bundled books end up as:

| Book | Result |
|---|---|
| The Prisoner | Valid |
| The Crystal Caverns | Invalid: section 900 leads to section 666, which isn't an ending and has no options |
| Pirates of the Jade Sea | Invalid: section 1 leads to the missing section 999, and to section 666 (no options) |
| Dragon Quest | Not loaded: the file is empty |

**Data quirks handled when loading:** section ids written as strings (`"500"`), consequence values as strings
(`"4"`), and the blank top-level `"type"` in Pirates, read as the book category. A file that can't be parsed is
logged and skipped.

**Card details.** The JSON files have no description, so the card shows the text of the opening section. The
section count replaces the "chapters" shown in the design. Reading time and tags aren't in the data, so they're
left out.
