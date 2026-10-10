package com.example.reservation.reservations;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;


    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationByID(@PathVariable("id") Long id){
        log.info("Called getReservationByID with id=" + id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationByID(id));
    }

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


    @PostMapping()
    public ResponseEntity<Reservation> createReservation(@RequestBody @Valid Reservation reservationToCreate){
        log.info("Called createReservation");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate));
    }


    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id){
        log.info("Called deleteReservation by id={}", id);
        reservationService.cancelReservation(id);

        return ResponseEntity.ok().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable("id") Long id, @RequestBody @Valid Reservation reservationToUpdate){
        log.info("Called updateReservation by id={}, reservation={}", id, reservationToUpdate);
        var updated = reservationService.updateReservation(id, reservationToUpdate);

        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable("id") Long id){
        log.info("Called approveReservation by id={}", id);
        var reservation = reservationService.approveReservation(id);

        return ResponseEntity.ok(reservation);
    }
}
