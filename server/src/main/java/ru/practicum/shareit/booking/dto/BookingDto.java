package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.shareit.booking.storage.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class BookingDto {

    Long id;

    LocalDateTime start;

    LocalDateTime end;

    ItemDto item;

    UserDto booker;

    BookingStatus status;
}
