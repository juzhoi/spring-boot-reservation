package com.example.reservation.reservations.availability;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {
}
