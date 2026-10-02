package com.example.reservation;


import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap = Map.of(
            1L, new Reservation(
                    1L,
                    100L,
                    40L,
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    ReservationStatus.PENDING
            ),
            2L, new Reservation(
                    2L,
                    105L,
                    48L,
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    ReservationStatus.PENDING
            ),
            3L, new Reservation(
                    3L,
                    102L,
                    41L,
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    ReservationStatus.PENDING
            )
    );


    public Reservation getReservationByID(Long id) {

        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Not found reservation by id=" + id);
        }

        return reservationMap.get(id);

    }

    public List<Reservation> findAllReservations() {
        return reservationMap.values().stream().toList();
    }
}
