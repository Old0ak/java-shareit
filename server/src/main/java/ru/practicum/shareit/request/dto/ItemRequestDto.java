package ru.practicum.shareit.request.dto;

import lombok.Value;
import ru.practicum.shareit.item.dto.ItemAnswerDto;

import java.time.LocalDateTime;
import java.util.List;

@Value
public class ItemRequestDto {

    Long id;

    String description;

    LocalDateTime created;

    List<ItemAnswerDto> items;
}
