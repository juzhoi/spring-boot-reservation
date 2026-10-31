package com.example.reservation.reservations;

import com.example.reservation.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void cleanDatabase() {
        reservationRepository.deleteAll();
    }

    @Test
    void approveReservation_whenDatesOverlapApproved_returns409() throws Exception {
        // Arrange: реальные данные в базе
        reservationRepository.save(new ReservationEntity(null, 1L, 5L,
                LocalDate.of(2099, 1, 10), LocalDate.of(2099, 1, 15), ReservationStatus.APPROVED));
        var pending = reservationRepository.save(new ReservationEntity(null, 2L, 5L,
                LocalDate.of(2099, 1, 12), LocalDate.of(2099, 1, 20), ReservationStatus.PENDING));

        // Act + Assert
        mockMvc.perform(post("/reservations/" + pending.getId() + "/approve"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Conflict"));
    }

    @Test
    void approveReservation_whenDatesOnlyTouch_returns200() throws Exception {
        // Arrange: реальные данные в базе
        reservationRepository.save(new ReservationEntity(null, 1L, 5L,
                LocalDate.of(2099, 1, 10), LocalDate.of(2099, 1, 15), ReservationStatus.APPROVED));
        var reservation = reservationRepository.save(new ReservationEntity(null, 2L, 5L,
                LocalDate.of(2099, 1, 15), LocalDate.of(2099, 1, 20), ReservationStatus.PENDING));


        mockMvc.perform(post("/reservations/" + reservation.getId() + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        var fromDb = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertEquals(ReservationStatus.APPROVED, fromDb.getStatus());
    }
}
