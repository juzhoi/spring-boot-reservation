package com.example.reservation.reservations;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ReservationController.class)
class ReservationControllerTest {
    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReservation_whenNotFound_returns404() throws Exception {
        when(reservationService.getReservationByID(99L))
                .thenThrow(new EntityNotFoundException("Not found reservation by id=99"));

        mockMvc.perform(get("/reservations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.errorDetailedMessage").value("Not found reservation by id=99"));
    }

    @Test
    void createReservation_whenEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.errorDetailedMessage").value(containsString("userId: must not be null")))
                .andExpect(jsonPath("$.errorDetailedMessage").value(containsString("roomId: must not be null")));

        verify(reservationService, never()).createReservation(any());
    }


    @Test
    void approveReservation_whenConflict_returns409() throws Exception {
        when(reservationService.approveReservation(99L))
                .thenThrow(new ReservationStateException("Cannot approve reservation: status= APPROVED"));

        mockMvc.perform(post("/reservations/99/approve"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Conflict"))
                .andExpect(jsonPath("$.errorDetailedMessage").value(containsString("Cannot approve reservation: status= APPROVED")));

        verify(reservationService, never()).createReservation(any());
    }

    @Test
    void createReservation_whenOK_returns201() throws Exception {
        var saved = new Reservation(
                1L,
                10L,
                5L,
                LocalDate.of(2099, 1, 10),
                LocalDate.of(2099, 1, 12),
                ReservationStatus.PENDING);

        when(reservationService.createReservation(any())).thenReturn(saved);

        mockMvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {"userId": 10, "roomId": 5, "startDate": "2099-01-10", "endDate": "2099-01-12"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void cancelReservation_whenSuccessful_returns204() throws Exception{
        mockMvc.perform(post("/reservations/99/cancel"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(99L);
    }


    @Test
    void deleteReservations_whenMethodNotAllowed_returns405() throws Exception{
        mockMvc.perform(delete("/reservations"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(header().string("Allow", containsString("POST")))
                .andExpect(jsonPath("$.message").value("Method Not Allowed"));

        verifyNoInteractions(reservationService);
    }
}