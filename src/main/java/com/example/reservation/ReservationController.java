package com.example.reservation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReservationController {


    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;




    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @GetMapping("/{id}")
    public Reservation getReservationByID(@PathVariable("id") Long id){
        log.info("Called getReservationByID");
        return reservationService.getReservationByID(id);
    }

    @GetMapping()
    public List<Reservation> getAllReservations(){
        log.info("Called getAllReservations");
        return reservationService.findAllReservations();
    }


}
