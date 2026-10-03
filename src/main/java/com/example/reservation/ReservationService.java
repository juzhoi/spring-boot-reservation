package com.example.reservation;


import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap;
    private final AtomicLong IdCounter;

    public ReservationService() {
        reservationMap = new HashMap<>();
        IdCounter = new AtomicLong();

    }

    public Reservation getReservationByID(Long id) {

        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Not found reservation by id=" + id);
        }

        return reservationMap.get(id);

    }

    public List<Reservation> findAllReservations() {
        return reservationMap.values().stream().toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if(reservationToCreate.id() != null){
            throw new IllegalArgumentException("ID should be empty");
        }

        if(reservationToCreate.status() != null){
            throw new IllegalArgumentException("Reservation status should be empty");
        }

        var newReservation = new Reservation(
                IdCounter.incrementAndGet(),
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(newReservation.id(),  newReservation);
        return newReservation;

    }

    public void deleteReservation(Long id) {
        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Not found reservation by id=" + id);
        }

        reservationMap.remove(id);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {

        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Not found reservation by id=" + id);
        }

        var reservation = reservationMap.get(id);

        if(reservation.status() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify reservation: status= " + reservationToUpdate.status());
        }

        var updatedReservation = new Reservation(
                reservation.id(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        reservationMap.put(reservation.id(), updatedReservation);

        return updatedReservation;
    }

    public Reservation approveReservation(Long id) {

        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Not found reservation by id=" + id);
        }

        var reservation = reservationMap.get(id);

        if(reservation.status() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status= " + reservation.status());
        }

        var isConflict = isReservationConflict(reservation);

        if (isConflict){
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }


        var approveReservation = new Reservation(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                ReservationStatus.APPROVED
        );

        reservationMap.put(reservation.id(), approveReservation);

        return approveReservation;



    }


    private boolean isReservationConflict(Reservation reservation){

        for(Reservation existingReservation : reservationMap.values()){
            if(reservation.id().equals(existingReservation.id())){
                continue;
            }
            if(!reservation.roomId().equals(existingReservation.roomId())){
                continue;
            }

            if(!existingReservation.status().equals(ReservationStatus.APPROVED)){
                continue;
            }

            if(reservation.startDate().isBefore(existingReservation.endDate())
                && existingReservation.startDate().isBefore(reservation.endDate())){
                return true;
            }
        }



        return false;
    }
}
