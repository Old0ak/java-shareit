package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.storage.BookingStatus;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper mapper;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingPostDto bookingPostDto) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingPostDto.getItemId());
        validateIsAvailable(item);
        validateDate(bookingPostDto);
        validateIsNotOwner(userId, item);

        Booking booking = mapper.toBooking(bookingPostDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        log.debug("Создание бронирования: id ={}", booking.getId());
        Booking createdBooking = bookingRepository.save(booking);
        return mapper.toBookingDto(createdBooking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);
        validateIsOwner(userId, booking.getItem());
        validateUserId(userId);
        validateStatus(booking.getStatus());

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        log.debug("Изменение статуса бронирования id={} владельцем id={} на {}",
                bookingId, userId, booking.getStatus());
        Booking updatedBooking = bookingRepository.save(booking);
        return mapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto findById(Long userId, Long bookingId) {
        validateUserId(userId);
        log.debug("Получение бронирования по id={}", bookingId);
        Booking booking = getBookingOrThrow(bookingId);
        log.debug("Проверка, что пользователь c id={} является владельцем вещи, либо автором бронирования", userId);
        validateBookerOrOwner(userId, booking);
        return mapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findAllByState(Long userId, BookingState state) {
        validateUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        log.debug("Поиск бронирований для автора id={} со статусом state={}", userId, state);
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.REJECTED);
        };
        return bookings.stream()
                .map(mapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> findAllByOwner(Long userId, BookingState state) {
        validateUserId(userId);
        validateUserHasItem(userId);
        LocalDateTime now = LocalDateTime.now();
        log.debug("Поиск бронирований вещей владельца id={} со статусом state={}", userId, state);
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now);
            case PAST -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.REJECTED);
        };
        return bookings.stream()
                .map(mapper::toBookingDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id = " + itemId + " не найдена"));
    }

    private void validateIsAvailable(Item item) {
        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ConditionsNotMetException("Вещь c id = " + item.getId() + " сейчас не доступна");
        }
    }

    private void validateDate(BookingPostDto bookingPostDto) {
        LocalDateTime start = bookingPostDto.getStart();
        LocalDateTime end = bookingPostDto.getEnd();
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new ConditionsNotMetException("Дата окончания бронирования не может раньше или равна дате начала");
        }
    }

    private void validateIsNotOwner(Long userId, Item item) {
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец вещи не может забронировать собственную вещь");
        }
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Бронирование с id + " + bookingId + " не найдено"));
    }

    private void validateIsOwner(Long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException("Пользователь с id = " + userId + " не является владельцем вещи");
        }
    }

    private void validateUserId(Long userId) {
        if (!userRepository.existsById(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

    }

    private void validateStatus(BookingStatus status) {
        if (status == BookingStatus.APPROVED) {
            throw new ConditionsNotMetException("Бронирование уже было подтверждено ранее");
        }
    }

    private void validateBookerOrOwner(Long userId, Booking booking) {
        Item item = booking.getItem();
        if (!item.getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new ConditionsNotMetException(
                    "Пользователь с id = " + userId + " не является ни владельцем вещи, ни автором бронирования");
        }
    }

    private void validateUserHasItem(Long userId) {
        if (itemRepository.findAllByOwnerId(userId).isEmpty()) {
            throw new ConditionsNotMetException(
                    "У пользователя с id = " + userId + " нет вещей для проверки бронирования");
        }
    }
}
