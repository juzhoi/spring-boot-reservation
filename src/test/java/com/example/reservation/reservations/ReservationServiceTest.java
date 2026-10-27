package com.example.reservation.reservations;

import com.example.reservation.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private static final LocalDate START = LocalDate.now().plusDays(1);
    private static final LocalDate END = LocalDate.now().plusDays(3);

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private ReservationAvailabilityService reservationAvailabilityService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void getReservationByID_whenNotFound_throwsEntityNotFound() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reservationService.getReservationByID(1L));
    }

    @Test
    void cancelReservation_whenApproved_throwsStateException() {
        var reservation = reservationWithStatus(ReservationStatus.APPROVED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationStateException.class, () -> reservationService.cancelReservation(1L));
        verify(reservationRepository, never()).setStatus(any(), any());
    }

    @Test
    void cancelReservation_whenCancelled_throwsStateException() {
        var reservation = reservationWithStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationStateException.class, () -> reservationService.cancelReservation(1L));
        verify(reservationRepository, never()).setStatus(any(), any());
    }


    @Test
    void cancelReservation_whenPending_setsStatusCancelled() {
        var reservation = reservationWithStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L);

        verify(reservationRepository).setStatus(1L, ReservationStatus.CANCELLED);
    }

    @Test
    void approveReservation_whenRoomAlreadyBooked_throwsStateException() {
        var reservation = reservationWithStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationAvailabilityService.isReservationAvailable(5L, START, END)).thenReturn(false);

        assertThrows(ReservationStateException.class, () -> reservationService.approveReservation(1L));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void approveReservation_whenNotPending_throwsStateException() {
        var reservation = reservationWithStatus(ReservationStatus.APPROVED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));


        assertThrows(ReservationStateException.class, () -> reservationService.approveReservation(1L));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void approveReservation_whenRoomIsAvailable_setsStatusApproved() {
        var reservation = reservationWithStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationAvailabilityService.isReservationAvailable(5L, START, END)).thenReturn(true);

        reservationService.approveReservation(1L);

        assertEquals(ReservationStatus.APPROVED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void createReservation_whenEndBeforeStart_throwsIllegalArgument() {
        var reservation = new Reservation(
                null, 10L, 5L,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(3),
                null
        );

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_whenStatusNotNull_throwsIllegalArgument() {
        var reservation = new Reservation(
                null, 10L, 5L,
                START,
                END,
                ReservationStatus.PENDING
        );

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_whenStatusNull_create() {
        var reservation = new Reservation(
                null, 10L, 5L,
                START,
                END,
                null
        );
        var entityToSave = new ReservationEntity(null, 10L, 5L, START, END, null);

        when(reservationMapper.toEntity(reservation)).thenReturn(entityToSave);

        reservationService.createReservation(reservation);

        assertEquals(ReservationStatus.PENDING, entityToSave.getStatus());

        verify(reservationRepository).save(entityToSave);
    }

    @Test
    void updateReservation_whenNotPending_throwsReservationStateException() {
        var reservation = reservationWithStatus(ReservationStatus.APPROVED);
        var reservationToUpdate = new Reservation(null, 10L, 5L, START, END, null);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationStateException.class, () -> reservationService.updateReservation(1L, reservationToUpdate));
        verify(reservationRepository, never()).save(any());
    }


    private ReservationEntity reservationWithStatus(ReservationStatus status) {
        return new ReservationEntity(1L, 10L, 5L, START, END, status);
    }

}