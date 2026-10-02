package com.example.reservation;


import java.time.LocalDate;
import java.time.LocalDateTime;


public record Reservation(
        Long id,
        Long userId,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        ReservationStatus status
) {

}
