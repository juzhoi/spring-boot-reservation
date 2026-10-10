package com.example.reservation.reservations.availability;

import com.example.reservation.reservations.ReservationRepository;
import com.example.reservation.reservations.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {

    private final ReservationRepository reservationRepository;

    public ReservationAvailabilityService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);



    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    ){

        if(!endDate.isAfter(startDate)){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        List<Long> conflictingIds = reservationRepository.findConflictReservationsIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED
        );

        if(conflictingIds.isEmpty()){
            return true;
        }

        log.info("Conflicting with ids={}", conflictingIds);
        return false;
    }
}
