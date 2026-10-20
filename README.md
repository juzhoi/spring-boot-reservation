# Reservation System

A REST API for booking rooms, built with Spring Boot and PostgreSQL.
Users create reservation requests, managers approve them, and the system prevents double bookings by checking date overlaps against already approved reservations.

## Tech Stack

- **Java 21**
- **Spring Boot 4.1** (Spring Web MVC, Spring Data JPA, Bean
Validation)
- **PostgreSQL** with Hibernate
- **Maven** (wrapper included)

## Features

- **Reservation lifecytarts as `PENDING` andcan be `APPROVED` or `CANCELLED`
- **Double-booking prean only be approved ifit doesn't overlap with another approved reservation for the same room
- **Availability checkfree for a date rangebefore booking
- **Filtering and pagions by room or user,with page size and page number
- **Input validation**n the past and invaliddate ranges are rejected with clear messages
- **Consistent error rre one JSON format with correct HTTP status codes (400, 404, 405, 409, 500)

### Business rules

| Action  | Allowed when       | Otherwise      |
|---------|-----------|
| Update  | status is PENDING  | 409 Conflict   |
| Approve | status is  APPROVED reservationfor the room | 409 Conflict |
| Cancel  | status is pproved reservationsmust be cancelled by a manager) |

## Getting Started

### Prerequisites

- Java 21
- Docker (for PostgreS

### Run

1. Start PostgreSQL:
```bash
   docker run --name mSWORD=root -p 5432:5432 -d postgres:17
 ```
2. Start the application:
 ```bash
   ./mvnw spring-boot:run
```
4. The API is available at `http://localhost:8080`.               
The database schema is created automatically on startup.          
## API        

| Method | Endpoint                            | Description      
|--------|-------------------------------------|----------------------------------------
| GET    | `/reservations`                     | List reservations(filters: `roomId`, `uumber`) | 200 |
| GET    | `/reservations/{id}`                | Get a reservationid
| POST   | `/reservations`                     | Create a reservat(status is set to PEND
| PUT    | `/reservations/{id}`                | Update a pending reservation
| POST   | `/reservations/{id}/approve`        | Approve a pendingreservation
| POST   | `/reservations/{id}/cancel`         | Cancel a pending reservation
| POST   | `/reservations/availability/check`  | Check if a room is
free for a date range

Pagination defaults: `0`.

### Example: create a

```bash
curl -X POST http://localhost:8080/reservations \
  -H "Content-Type: ap
  -d '{"userId": 1, "roomId": 5, "startDate": "2026-12-08", "endDate":
"2026-12-20"}'
```

```json
{
  "id": 1,
  "userId": 1,
  "roomId": 5,
  "startDate": "2026-1
  "endDate": "2026-12-20",
  "status": "PENDING"
}
```

## Error Handling

All errors return the reating a reservationwith an empty body:

```json
{
  "message": "Bad Request",
  "errorDetailedMessagull; startDate: mustnot be null; endDate: must not be null; roomId: must not be null",
  "errorTime": "2026-1
}
```

Internal server errorstraces or internaldetails to the client.

## Project Structure

```
src/main/java/com/exam
├── reservations/          # controller, service, repository, entity,
mapper
│   └── availability/      # room availability check
└── web/              ndling and error DTO
```

## Known Limitations

- **No authentication yet**: any client can approve or cancel
reservations
- **Race condition on approve**: two concurrent approvals for
overlapping dates can d with database-levellocking

## Roadmap

- [ ] OpenAPI / Swagger UI documentation
- [ ] Unit tests (JUniion tests(Testcontainers)
- [ ] Docker Compose f
- [ ] Authentication and roles with Spring Security and JWT
- [ ] Database migrati
- [ ] Separate request/response DTOs and paginated responses
- [ ] CI/CD with GitHument
