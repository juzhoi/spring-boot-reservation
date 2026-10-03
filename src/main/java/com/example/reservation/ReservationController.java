package com.example.reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

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

        try{
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservationService.getReservationByID(id));

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations(){
        log.info("Called getAllReservations");
        return ResponseEntity.ok(reservationService.findAllReservations());
    }


    @PostMapping()
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservationToCreate){
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id){
        log.info("Called deleteReservation by id={}", id);

        try{
            reservationService.deleteReservation(id);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable("id") Long id, @RequestBody Reservation reservationToUpdate){
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
