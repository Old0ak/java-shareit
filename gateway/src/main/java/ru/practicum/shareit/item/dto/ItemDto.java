package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ItemDto {

    Long id;

    @NotBlank
    String name;

    @Size(max = 200)
    String description;

    Boolean available;

    Long requestId;

    BookingShortDto lastBooking;
    BookingShortDto nextBooking;

    List<CommentDto> comments;

    @Value
    public static class BookingShortDto {
        Long id;
        Long bookerId;
    }
}
