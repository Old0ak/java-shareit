package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class BookingPostDto {

    @NotNull
    Long itemId;

    @NotNull
    @FutureOrPresent
    LocalDateTime start;

    @NotNull
    @Future
    LocalDateTime end;
}
