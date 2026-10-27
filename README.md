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
