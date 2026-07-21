package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    // добавление нового запроса на бронирование
    @PostMapping
    public BookingDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody BookingPostDto bookingPostDto) {
        log.info("Получен запрос от пользователя: id={} на добавление нового бронирование", userId);
        BookingDto createdBooking = bookingService.create(userId, bookingPostDto);
        log.info("Успешно добавлено бронирование: id={}", createdBooking.getId());
        return createdBooking;
    }

    // подтверждение или отклонение запроса на бронирование
    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam Boolean approved) {
        log.info("Получен запрос от пользователя: id={} на подтверждение (approved={}) бронирования id={}",
                userId, approved, bookingId);
        BookingDto updatedBooking = bookingService.approve(userId, bookingId, approved);
        log.info("Статус бронирования id={} успешно обновлен владельцем: id={}", bookingId, userId);
        return updatedBooking;
    }

    // получение данных о бронировании по id
    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long bookingId) {
        log.info("Получен запрос от пользователя: id={} на получение данных о бронировании id={}", userId, bookingId);
        BookingDto bookingDto = bookingService.findById(userId, bookingId);
        log.info("Успешно получены данные о бронирование id={}", bookingId);
        return bookingDto;
    }

    // получение списка всех бронирований пользователя
    @GetMapping
    public List<BookingDto> findAllBookingsByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получен запрос от пользователя: id={} на получение списка своих бронирований, включая: state={}",
                userId, state);
        List<BookingDto> bookings = bookingService.findAllByState(userId, state);
        log.info("Успешно получен список бронирования в размере: {} пользователя: id={}, включая: state={}",
                bookings.size(), userId, state);
        return bookings;
    }

    // получение списка бронирований для всех вещей владельца
    @GetMapping("/owner")
    public List<BookingDto> findAllBookingsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получен запрос от владельца: id={} на получение списка бронирований для всех вещей", userId);
        List<BookingDto> bookings = bookingService.findAllByOwner(userId, state);
        log.info("Успешно получен список бронирования всех вещей владельца: id={} в размере: {}",
                userId, bookings.size());
        return bookings;
    }

}
