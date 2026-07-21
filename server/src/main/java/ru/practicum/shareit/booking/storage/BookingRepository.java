package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // проверка, что пользователь арендовал вещь, и ее аренда уже завершилась
    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime now);

    // ==================================================
    // МЕТОДЫ ДЛЯ АРЕНДАТОРА (Booker) – поиск по bookerId
    // ==================================================

    // STATE = ALL: ВСЕ бронирования пользователя
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    // STATE = CURRENT: ТЕКУЩИЕ бронирования пользователя
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime startBefore,
            LocalDateTime endAfter);

    // STATE = PAST: ПРОШЕДШИЕ бронирования пользователя
    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime endBefore);

    // STATE = FUTURE: БУДУЩИЕ бронирования пользователя
    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime startAfter);

    // STATE = WAITING или REJECTED: ОЖИДАЮЩИЕ подтверждения или ОТКЛОНЕННЫЕ бронирования пользователя
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    //=================================================================
    // МЕТОДЫ ДЛЯ ВЛАДЕЛЬЦА ВЕЩЕЙ (item owner) - поиск по item.owner.id
    //=================================================================

    // STATE = ALL: ВСЕ бронирования вещей этого владельца
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    // STATE = CURRENT: ТЕКУЩИЕ бронирования вещей этого владельца
    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId,
            LocalDateTime startBefore,
            LocalDateTime endAfter);

    // STATE = PAST: ПРОШЕДШИЕ бронирования вещей этого владельца
    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime endBefore);

    // STATE = FUTURE: БУДУЩИЕ бронирования вещей этого владельца
    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime startAfter);

    // STATE = WAITING или REJECTED: Фильтрация бронирований вещей по статусу
    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    // ==============================================================
    // МЕТОДЫ ПОИСКА БРОНИРОВАНИЙ ДЛЯ ВЕЩЕЙ (item) - поиск по item.id
    // ==============================================================

    // поиск APPROVED бронирований для вещи, которы начались в прошлом или идут сейчас (для lastBooking)
    List<Booking> findAllByItemIdAndStatusAndStartBeforeOrderByStartDesc(
            Long itemId,
            BookingStatus status,
            LocalDateTime now);

    // поиск APPROVED бронирований для вещи, которы начнутся в будущем (для nextBooking)
    List<Booking> findAllByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId,
            BookingStatus status,
            LocalDateTime now);

}