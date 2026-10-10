package com.example.reservation.reservations.availability;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CheckAvailabilityRequest(
        @NotNull
        Long roomId,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {
}
