package com.example.reservation.reservations.availability;

import com.example.reservation.web.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Availability", description = "Check whether a room is free for a given date range")
@RestController
@RequestMapping("/reservations/availability")
public class ReservationAvailabilityController {

    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationAvailabilityController(ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationAvailabilityService = reservationAvailabilityService;
    }

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability status returned"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @Operation(
            summary = "Check room availability",
            description = "Returns AVAILABLE if no approved reservation "
                    + "overlaps the given date range, otherwise RESERVED."
    )
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
