package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ItemDto {

    Long id;

    @NotBlank
    String name;

    @Size(max = 200)
    String description;

    Boolean available;

    Long requestId;
}
