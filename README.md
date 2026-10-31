# Reservation System

A REST API for booking rooms, built with Spring Boot and PostgreSQL.
Users create reservation requests, managers approve them, and the system prevents double bookings by checking date overlaps against already approved reservations.

## Tech Stack

- **Java 21**
- **Spring Boot 4.1** (Spring Web MVC, Spring Data JPA, Bean Validation)
- **PostgreSQL** with Hibernate
- **springdoc-openapi** (Swagger UI)
- **Maven** (wrapper included)
- **JUnit 5**, **Mockito**, **MockMvc**, **Testcontainers** for testing

## Features

- **Reservation lifecycle**: every reservation starts as `PENDING` and can be `APPROVED` or `CANCELLED`
- **Double-booking prevention**: a reservation can only be approved if it doesn't overlap with another approved reservation for the same room
- **Availability check**: ask whether a room is free for a date range before booking
- **Filtering and pagination**: search reservations by room or user, with page size and page number
- **Input validation**: required fields, dates in the past and invalid date ranges are rejected with clear messages
- **Consistent error responses**: all errors share one JSON format with correct HTTP status codes (400, 404, 405, 409, 500)

### Business rules

| Action  | Allowed when | Otherwise |
|---------|--------------|-----------|
| Update  | status is `PENDING` | 409 Conflict |
| Approve | status is `PENDING` and no overlapping `APPROVED` reservation exists for the room | 409 Conflict |
| Cancel  | status is `PENDING` | 409 Conflict (approved reservations must be cancelled by a manager) |

## Getting Started

### Prerequisites

- Docker

### Run

1. Create a `.env` file from the template and set a password:
   ```bash
   cp .env.example .env
   ```
2. Start the database and the application:
   ```bash
   docker compose up -d --build
   ```
3. The API is available at `http://localhost:8080`.
4. Interactive API docs: `http://localhost:8080/swagger-ui.html`

The database schema is created automatically on startup.

### Run locally from the IDE

Requires Java 21. Start only the database, then run `ReservationSystemApplication`
with the `POSTGRES_PASSWORD` environment variable set:
```bash
docker compose up -d db
```

### Tests

```bash
./mvnw test
```

Integration tests start PostgreSQL automatically with Testcontainers, so only Docker needs to be running.

## API
Full interactive documentation with request/response schemas is available in Swagger UI (see [Run](#run)).

| Method | Endpoint | Description | Success |
|--------|----------|-------------|---------|
| GET  | `/reservations` | List reservations (filters: `roomId`, `userId`, `pageSize`, `pageNumber`) | 200 |
| GET  | `/reservations/{id}` | Get a reservation by id | 200 |
| POST | `/reservations` | Create a reservation (status is set to `PENDING`) | 201 |
| PUT  | `/reservations/{id}` | Update a pending reservation | 200 |
| POST | `/reservations/{id}/approve` | Approve a pending reservation | 200 |
| POST | `/reservations/{id}/cancel` | Cancel a pending reservation | 204 |
| POST | `/reservations/availability/check` | Check if a room is free for a date range | 200 |

Pagination defaults: `pageSize=10`, `pageNumber=0`.

### Example: create a reservation

```bash
curl -X POST http://localhost:8080/reservations \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "roomId": 5, "startDate": "2026-12-08", "endDate": "2026-12-20"}'
```

```json
{
  "id": 1,
  "userId": 1,
  "roomId": 5,
  "startDate": "2026-12-08",
  "endDate": "2026-12-20",
  "status": "PENDING"
}
```

## Error Handling

All errors return the same structure. Example: creating a reservation with an empty body:

```json
{
  "message": "Bad Request",
  "errorDetailedMessage": "userId: must not be null; startDate: must not be null; endDate: must not be null; roomId: must not be null",
  "errorTime": "2026-10-20T15:53:42.40016"
}
```

Internal server errors (500) never expose stack traces or internal details to the client.

## Project Structure

```
src/main/java/com/example/reservation
├── reservations/          # controller, service, repository, entity, mapper
│   └── availability/      # room availability check
└── web/                   # global exception handling and error DTO
```

## Known Limitations

- **No authentication yet**: any client can approve or cancel reservations
- **Race condition on approve**: two concurrent approvals for overlapping dates can both succeed; will be fixed with database-level locking

## Roadmap

- [x] OpenAPI / Swagger UI documentation
- [x] Unit tests (JUnit 5, Mockito) and integration tests (Testcontainers)
- [x] Docker Compose for one-command startup
- [ ] Authentication and roles with Spring Security and JWT
- [ ] Database migrations with Flyway
- [ ] Separate request/response DTOs and paginated responses
- [ ] CI/CD with GitHub Actions and cloud deployment
