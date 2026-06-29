package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request.id", source = "requestId")
    Item toItem(ItemDto itemDto);

    @Mapping(target = "requestId", source = "request.id")
    ItemDto toItemDto(Item item);
}
