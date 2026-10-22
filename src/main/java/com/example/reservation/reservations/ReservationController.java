package com.example.reservation.reservations;

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
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Reservations", description = "Create, view, update, approve and cancel room reservations")
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;


    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation by id returned"),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @Operation(summary = "Get a reservation by id")
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationByID(@PathVariable("id") Long id){
        log.info("Called getReservationByID with id=" + id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationByID(id));
    }


    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservations by filter returned"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @Operation(
            summary = "List reservations",
            description = "Optional filters by room and user. "
                    + "Paginated: pageSize defaults to 10, pageNumber to 0."
    )
    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations(
            @RequestParam(name = "roomId", required = false) Long roomId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ){
        log.info("Called getAllReservations");
        var filter = new ReservationSearchFilter(roomId, userId, pageSize, pageNumber);

        return ResponseEntity.ok(reservationService.searchAllByFilter(filter));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @Operation(
            summary = "Create a reservation",
            description = "New reservations always start with status PENDING. "
                    + "End date must be after start date."
    )
    @PostMapping()
    public ResponseEntity<Reservation> createReservation(@RequestBody @Valid Reservation reservationToCreate){
        log.info("Called createReservation");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate));
    }


    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reservation cancelled"),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Reservation is not PENDING",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @Operation(
            summary = "Cancel a pending reservation",
            description = "Only PENDING reservations can be cancelled. "
                    + "Approved reservations must be cancelled by a manager."
    )
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable("id") Long id){
        log.info("Called cancelReservation by id={}", id);
        reservationService.cancelReservation(id);

        return ResponseEntity.noContent().build();
    }


    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation updated"),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Reservation is not PENDING",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @Operation(
            summary = "Update a pending reservation",
            description = "Only PENDING reservations can be updated."
    )
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable("id") Long id, @RequestBody @Valid Reservation reservationToUpdate){
        log.info("Called updateReservation by id={}, reservation={}", id, reservationToUpdate);
        var updated = reservationService.updateReservation(id, reservationToUpdate);

        return ResponseEntity.ok(updated);
    }



    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation approved"),
            @ApiResponse(responseCode = "404", description = "Reservation not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Not PENDING or room already booked",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @Operation(
            summary = "Approve a pending reservation",
            description = "Only PENDING reservations can be approved. "
                    + "Fails if the room already has an approved reservation "
                    + "with overlapping dates."
    )
    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable("id") Long id){
        log.info("Called approveReservation by id={}", id);
        var reservation = reservationService.approveReservation(id);

        return ResponseEntity.ok(reservation);
    }
}
