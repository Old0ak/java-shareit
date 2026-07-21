package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingPostDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingPostDto bookingPostDto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto findById(Long userId, Long bookingId);

    List<BookingDto> findAllByState(Long userId, BookingState state);

    List<BookingDto> findAllByOwner(Long userId, BookingState state);
}
