package ru.practicum.shareit.booking.storage;

public enum BookingStatus {
    WAITING,    // новое бронирование, ОЖИДАЕТ одобрения
    APPROVED,   // бронирование ПОДТВЕРЖДЕНО владельцем
    REJECTED,   // бронирование ОТКЛОНЕНО владельцем
    CANCELED    // бронирование ОТМЕНЕНО создателем
}
