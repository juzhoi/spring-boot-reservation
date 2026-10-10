package com.example.reservation.reservations;

import com.example.reservation.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationService(ReservationRepository reservationRepository, ReservationMapper reservationMapper, ReservationAvailabilityService reservationAvailabilityService) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.reservationAvailabilityService = reservationAvailabilityService;
    }


    public Reservation getReservationByID(Long id) {

        ReservationEntity entity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id=" + id));

        return reservationMapper.toDomain(entity);
    }


    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ) {
        int pageSize = filter.pageSize() != null
                ? filter.pageSize() : 10;

        int pageNumber = filter.pageNumber() != null
                ? filter.pageNumber() : 0;

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> allEnteties = reservationRepository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEnteties.stream().map(reservationMapper::toDomain).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if(reservationToCreate.status() != null){
            throw new IllegalArgumentException("Reservation status should be empty");
        }

        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())){
            throw new IllegalArgumentException("Start date must be one day earlier than end date");
        }

        var entityToSave = reservationMapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        entityToSave.setId(null);


        var savedEntity = reservationRepository.save(entityToSave);

        return reservationMapper.toDomain(savedEntity);
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

        var reservationToSave = reservationMapper.toEntity(reservationToUpdate);
        reservationToSave.setStatus(ReservationStatus.PENDING);
        reservationToSave.setId(reservation.getId());

        var updatedReservation = reservationRepository.save(reservationToSave);

        return reservationMapper.toDomain(updatedReservation);
    }


    public Reservation approveReservation(Long id) {
        var reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id=" + id));

        if(reservation.getStatus() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status= " + reservation.getStatus());
        }

        var isAvailableToApprove = reservationAvailabilityService.isReservationAvailable(
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate()
        );

        if (!isAvailableToApprove){
            throw new IllegalStateException("Cannot approve reservation because of conflict");
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        reservationRepository.save(reservation);

        return reservationMapper.toDomain(reservation);
    }
}
