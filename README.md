# Employee Mongo CRUD (Spring Boot + MongoDB + JWT)

[![Java CI with Maven](https://github.com/bindian0509/mongo/actions/workflows/maven.yml/badge.svg)](https://github.com/bindian0509/mongo/actions/workflows/maven.yml)

Secure REST API for employee CRUD operations backed by MongoDB. Authentication uses JWT access tokens plus refresh tokens; all API routes are protected except login and refresh.

## Prerequisites

- Java 21+
- Maven Wrapper (included: `./mvnw`)
- Docker & Docker Compose (for MongoDB)

## Getting Started

1. **Start MongoDB** (with authentication):

   ```bash
   docker-compose up -d
   ```

   This starts MongoDB with credentials `admin/changeit` on port 27017.

2. **Run tests**:

   ```bash
   ./mvnw test
   ```

3. **Run the app**:
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
   { "username": "admin", "password": "changeit" }
   ```
   Response: `{ "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer" }`
2. **Authenticated requests** – Send `Authorization: Bearer <accessToken>` on all `/api/employees/**` routes.
3. **Refresh** – `POST /api/auth/refresh` with body:
   ```json
   { "refreshToken": "<refreshToken>" }
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

## API Docs (Swagger / OpenAPI)

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Use the “Authorize” button in Swagger UI and paste `Bearer <accessToken>` to call secured endpoints.

## Postman Collection

Import `postman/employee-mongo.postman_collection.json`.

- Run **Login (get JWT)** to store `accessToken` and `refreshToken` in collection variables.
- Use **Refresh token** to rotate tokens when access expires.
- All employee requests automatically send `Authorization: Bearer {{accessToken}}`.

## Tests

The project includes both unit tests and RestAssured-based API integration tests.

**Run all tests** (requires MongoDB running):

```bash
./mvnw test
```

**Run only API tests**:

```bash
./mvnw test -Dtest="EmployeeApi*"
```

### Test Coverage

- **Unit tests**: `EmployeeServiceTest`, `EmployeeControllerValidationTest`
- **API tests** (RestAssured):
  - `EmployeeApiCrudTest` — Full CRUD lifecycle with JWT auth
  - `EmployeeApiListingTest` — List/search scenarios
  - `EmployeeApiValidationTest` — Validation errors, duplicate email (409)

### CI/CD

GitHub Actions runs tests automatically on push/PR to `main`. The workflow starts a MongoDB service container with matching credentials.

## AWS ECS Deployment

For production deployment on AWS ECS with DocumentDB (MongoDB-compatible), see the detailed guide:

📘 **[Deployment Guide](.aws/DEPLOYMENT.md)**

Key files for deployment:

- `Dockerfile` - Multi-stage build for containerization
- `.github/workflows/deploy-ecs.yml` - CI/CD pipeline
- `.aws/task-definition.json` - ECS task configuration
- `.aws/infrastructure.tf` - Terraform IaC for AWS resources

## Notes

- App is stateless (JWT); CSRF is disabled by design for API use.
- Update secrets and credentials in production; defaults are for local development only.
- MongoDB credentials: `admin/changeit` (for local dev and tests)
