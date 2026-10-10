package com.example.reservation.reservations.availability;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {

    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationAvailabilityController(ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);



    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @RequestBody @Valid CheckAvailabilityRequest request
    ){
        log.info("Called method checkAvailability: request={}", request);
        boolean isAvailable = reservationAvailabilityService.isReservationAvailable(
                request.roomId(),
                request.startDate(),
                request.endDate()
        );


        var message = isAvailable
                ? "Room available to reservation"
                : "Room not available to reservation" ;

        var status = isAvailable
                ? AvailabilityStatus.AVAILABLE
                : AvailabilityStatus.RESERVED;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new CheckAvailabilityResponse(message, status));

    }
}
