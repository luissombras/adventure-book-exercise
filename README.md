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
| GET | `/api/books` | Library, with each book's validation errors. |
| POST | `/api/games` `{"bookId"}` | Start a game at the book's beginning. `201`, or `404` / `422` (book unknown / not playable). |
| GET | `/api/games/{id}` | Current state of a game. |
| POST | `/api/games/{id}/choices` `{"optionIndex"}` | Take an option of the current section (0-based). `409` if the section has no such option or the game is over. |

A game is returned as `{ id, bookId, bookTitle, status, health, maxHealth, consequence, section }`, where `status` is
`IN_PROGRESS`, `FINISHED` or `DEAD`, and `consequence` is the result of the last choice (`{ text, healthChange }`) or
`null`.

Errors use Problem Details (`application/problem+json`); a `422` also lists the book's validation errors.

## Decisions

**Book validation.** A book is invalid when it has no beginning or more than one, has no ending, has an option
leading to a section that doesn't exist, or has a non-ending section without options. For that last rule only
sections reachable from the beginning are considered: a dead section nobody can get to can't block a game. Two
sections with the same id are also an error, since an option pointing at that id would be ambiguous.

Validation is a single O(sections + options) pass plus a graph traversal, and each book is validated once and
cached, since books don't change after loading.

**Invalid books stay in the library**, marked as unplayable with the reasons, instead of being hidden.

**Search and filters run in the browser.** The whole library fits in one response, so the page loads it once
and filters it locally. That would move to the API, with pagination, if the library grew large.

With these rules, the bundled books end up as:

| Book | Result |
|---|---|
| The Prisoner | Valid |
| The Crystal Caverns | Invalid: section 900 leads to section 666, which isn't an ending and has no options |
| Pirates of the Jade Sea | Invalid: section 1 leads to the missing section 999, and to section 666 (no options) |
| Dragon Quest | Not loaded: the file is empty |

**Data quirks handled when loading:** section ids written as strings (`"500"`), consequence values as strings
(`"4"`). Unknown fields such as the blank top-level `"type"` in Pirates are ignored. A file that can't be parsed
is logged and skipped.

**Card details.** The JSON files have no description, so the card shows the text of the opening section. The
section count replaces the "chapters" shown in the design. Reading time and tags aren't in the data, so they're
left out, and so is the category filter: no book has a category.

**The game runs on the server.** The client only sends which option was picked; the server checks it against the
current section and decides where the player goes. That keeps the rules in one place and testable in Java, and it
is where health and consequences (objective 3) and saved progress (objective 4) will plug in. Games are kept in
memory, so they are lost when the backend restarts, and refreshing the game page starts a new game until saving
exists. Games are never deleted either: fine for a local exercise, but a real deployment would expire them after a
while of inactivity or store them in a database. A choice is applied atomically (`ConcurrentHashMap.computeIfPresent`),
so two requests for the same game can't both act on the same section.

**Health.** The player starts with 10 health. `LOSE_HEALTH` and `GAIN_HEALTH` consequences move it, and it stays
between 0 and 10: the brief only gives the starting value, and a cap keeps potions from stacking up. At 0 the player
dies on the section where they made the choice, without moving to the option's section, since that text assumes they
survived. Death wins over an ending: losing the last health point on the way to an `END` section is still death.

**Sections have no titles in the data**, so the game screen shows the section number instead of the titles in the
design.
