package com.example.reservation;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }


    public Reservation getReservationByID(Long id) {

        ReservationEntity entity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id=" + id));

        return toDomainReservation(entity);
    }


    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEnteties = reservationRepository.findAll();

        return allEnteties.stream().map(this::toDomainReservation).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if(reservationToCreate.status() != null){
            throw new IllegalArgumentException("Reservation status should be empty");
        }

        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );

        var savedEntity = reservationRepository.save(entityToSave);

        return toDomainReservation(savedEntity);
    }

    @Transactional
    public void cancelReservation(Long id) {

        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id=" + id));

        if(reservation.getStatus().equals(ReservationStatus.APPROVED)){
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
        }

        if(reservation.getStatus().equals(ReservationStatus.CANCELLED)){
            throw new IllegalStateException("Cannot cancel the reservation. Reservation was already cancelled");
        }


        log.info("Successfully cancelled reservation by id={}", id);

        reservationRepository.setStatus(id, ReservationStatus.CANCELLED);
    }


    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id=" + id));

        if(reservation.getStatus()!= ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot modify reservation: status= " + reservation.getStatus());
        }

        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        var reservationToSave = new ReservationEntity(
                reservation.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );

        var updatedReservation = reservationRepository.save(reservationToSave);

        return toDomainReservation(updatedReservation);
    }


    public Reservation approveReservation(Long id) {
        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id=" + id));

        if(reservation.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status= " + reservation.getStatus());
        }

        var isConflict = isReservationConflict(reservation);

        if (isConflict){
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservation);

        return toDomainReservation(reservation);
    }


    private boolean isReservationConflict(ReservationEntity reservation){
        var AllReservations = reservationRepository.findAll();

        for(ReservationEntity existingReservation : AllReservations){
            if(reservation.getId().equals(existingReservation.getId())){
                continue;
            }

            if(!reservation.getRoomId().equals(existingReservation.getRoomId())){
                continue;
            }

            if(!existingReservation.getStatus().equals(ReservationStatus.APPROVED)){
                continue;
            }

            if(reservation.getStartDate().isBefore(existingReservation.getEndDate())
                && existingReservation.getStartDate().isBefore(reservation.getEndDate())){
                return true;
            }
        }

        return false;
    }


    private Reservation toDomainReservation(ReservationEntity reservationEntity){
        return new Reservation(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus()
        );
    }

}
