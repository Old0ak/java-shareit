package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final String headerParam = "X-Sharer-User-Id";

    // =============
    // ENDPOINT: add
    // =============

    @Test
    void shouldCreateBookingSuccess() throws Exception {
        BookingPostDto postDto = BookingPostDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        BookingDto responseDto = BookingDto.builder().id(1L).build();

        Mockito.when(bookingService.create(anyLong(), any(BookingPostDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header(headerParam, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldThrowExceptionWhenIncorrectDate() throws Exception {
        BookingPostDto postDto = BookingPostDto.builder().itemId(1L).build();

        Mockito.when(bookingService.create(anyLong(), any(BookingPostDto.class)))
                .thenThrow(new ConditionsNotMetException("Некорректная дата"));

        mockMvc.perform(post("/bookings")
                        .header(headerParam, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest());
    }

    // =================
    // ENDPOINT: approve
    // =================

    @Test
    void shouldApproveBookingSuccess() throws Exception {
        BookingDto responseDto = BookingDto.builder().id(1L).build();
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(headerParam, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ====================
    // ENDPOINT: getBooking
    // ====================

    @Test
    void shouldReturnBookingById() throws Exception {
        BookingDto responseDto = BookingDto.builder().id(1L).build();
        Mockito.when(bookingService.findById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header(headerParam, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldThrowExceptionWhenBookingNotFound() throws Exception {
        Mockito.when(bookingService.findById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header(headerParam, 1L))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // ENDPOINT: findAllBookingsByUser
    // ===============================

    @Test
    void shouldFindAllBookingsByUserWithStateCurrent() throws Exception {
        BookingDto responseDto = BookingDto.builder().id(1L).build();
        Mockito.when(bookingService.findAllByState(anyLong(), any(BookingState.class)))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header(headerParam, 1L)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // ================================
    // ENDPOINT: findAllBookingsByOwner
    // ================================

    @Test
    void shouldFindAllBookingsByOwnerWithStateAll() throws Exception {
        BookingDto responseDto = BookingDto.builder().id(2L).build();
        Mockito.when(bookingService.findAllByOwner(anyLong(), any(BookingState.class)))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header(headerParam, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }
}
