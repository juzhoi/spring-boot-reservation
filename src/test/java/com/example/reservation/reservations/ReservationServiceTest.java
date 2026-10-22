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
        var reservation = new ReservationEntity(
                1L, 10L, 5L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                ReservationStatus.APPROVED
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationStateException.class, () -> reservationService.cancelReservation(1L));
        verify(reservationRepository, never()).setStatus(any(), any());
    }


    @Test
    void cancelReservation_whenPending_setsStatusCancelled() {
        var reservation = new ReservationEntity(
                1L, 10L, 5L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                ReservationStatus.PENDING
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L);

        verify(reservationRepository).setStatus(1L, ReservationStatus.CANCELLED);
    }

    @Test
    void approveReservation_whenRoomAlreadyBooked_throwsStateException() {
        var start = LocalDate.now().plusDays(1);
        var end = LocalDate.now().plusDays(3);
        var reservation = new ReservationEntity(1L, 10L, 5L, start, end, ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationAvailabilityService.isReservationAvailable(5L, start, end)).thenReturn(false);

        assertThrows(ReservationStateException.class, () -> reservationService.approveReservation(1L));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void approveReservation_whenRoomIsAvailable_setsStatusApproved() {
        var start = LocalDate.now().plusDays(1);
        var end = LocalDate.now().plusDays(3);
        var reservation = new ReservationEntity(1L, 10L, 5L, start, end, ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationAvailabilityService.isReservationAvailable(5L, start, end)).thenReturn(true);

        reservationService.approveReservation(1L);

        assertEquals(ReservationStatus.APPROVED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }
}