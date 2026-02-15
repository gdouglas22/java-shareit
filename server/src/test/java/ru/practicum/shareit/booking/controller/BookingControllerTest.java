package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_returnsCreatedBooking() throws Exception {
        BookingCreateRequestDto request = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2026, 2, 14, 10, 0))
                .end(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build();
        BookingResponseDto response = BookingResponseDto.builder().id(10L).status(BookingStatus.WAITING).build();
        when(bookingService.createBooking(eq(5L), eq(request))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve_returnsUpdatedBooking() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder().id(11L).status(BookingStatus.APPROVED).build();
        when(bookingService.approveBooking(2L, 11L, true)).thenReturn(response);

        mockMvc.perform(patch("/bookings/11")
                        .header("X-Sharer-User-Id", 2L)
                        .queryParam("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById_returnsBooking() throws Exception {
        BookingResponseDto response = BookingResponseDto.builder().id(12L).status(BookingStatus.REJECTED).build();
        when(bookingService.getBooking(3L, 12L)).thenReturn(response);

        mockMvc.perform(get("/bookings/12")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12));
    }

    @Test
    void getBookings_passesState() throws Exception {
        when(bookingService.getBookings(4L, "CURRENT")).thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 4L)
                        .queryParam("state", "CURRENT"))
                .andExpect(status().isOk());

        verify(bookingService).getBookings(4L, "CURRENT");
    }

    @Test
    void getOwnerBookings_usesDefaultStateAll() throws Exception {
        when(bookingService.getOwnerBookings(9L, "ALL")).thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 9L))
                .andExpect(status().isOk());

        verify(bookingService).getOwnerBookings(9L, "ALL");
    }
}

