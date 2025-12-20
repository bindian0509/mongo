# Employee Mongo CRUD (Spring Boot + MongoDB + JWT)

Secure REST API for employee CRUD operations backed by MongoDB. Authentication uses JWT access tokens plus refresh tokens; all API routes are protected except login and refresh.

## Prerequisites
- Java 21+
- Maven Wrapper (included: `./mvnw`)
- MongoDB reachable at the host/port in `application.properties` (Docker Compose helper available: `compose.yaml`)

## Getting Started
1. Install dependencies and run tests:
   ```bash
   ./mvnw test
   ```
2. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   Default port: `8080`.

## Environment / Configuration
Configure via env vars (defaults live in `src/main/resources/application.properties`):
- `MONGO_HOST`, `MONGO_PORT`, `MONGO_DATABASE`, `MONGO_USERNAME`, `MONGO_PASSWORD`, `MONGO_AUTH_DB`
- `AUTH_USERNAME`, `AUTH_PASSWORD` (in-memory user for login)
- `JWT_SECRET` (HS256 secret key)
- `JWT_EXPIRATION_MS` (access token TTL in ms)
- `JWT_REFRESH_EXPIRATION_MS` (refresh token TTL in ms)

## Auth Flow
1. **Login** – `POST /api/auth/login` with body:
   ```json
   {"username": "admin", "password": "changeit"}
   ```
   Response: `{ "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer" }`
2. **Authenticated requests** – Send `Authorization: Bearer <accessToken>` on all `/api/employees/**` routes.
3. **Refresh** – `POST /api/auth/refresh` with body:
   ```json
   {"refreshToken": "<refreshToken>"}
   ```
   Response returns a new access/refresh pair.

## API Quick Reference
- `POST /api/auth/login` – obtain tokens
- `POST /api/auth/refresh` – rotate tokens
- `GET /api/employees` – list
- `GET /api/employees/{id}` – get by id
- `POST /api/employees` – create
- `PUT /api/employees/{id}` – update
- `DELETE /api/employees/{id}` – delete

## Postman Collection
Import `postman/employee-mongo.postman_collection.json`.
- Run **Login (get JWT)** to store `accessToken` and `refreshToken` in collection variables.
- Use **Refresh token** to rotate tokens when access expires.
- All employee requests automatically send `Authorization: Bearer {{accessToken}}`.

## Tests
Run all tests:
```bash
./mvnw test
```

## Notes
- App is stateless (JWT); CSRF is disabled by design for API use.
- Update secrets and credentials in production; defaults are for local development only.

