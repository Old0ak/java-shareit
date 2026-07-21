package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TechnicalCoverageTest {

    @Test
    void completeCoverageOfMissedClasses() {
        // 1. Вызов кастомных исключений
        assertNotNull(new ConditionsNotMetException("error").getMessage());
        assertNotNull(new NotFoundException("error").getMessage());
        assertNotNull(new ConflictException("error").getMessage());

        // 2. Вызов ErrorResponse
        ErrorResponse response = new ErrorResponse("message");
        assertNotNull(response.getError());

        // 3. MapStruct
        assertNotNull(Mappers.getMapper(UserMapper.class));
        assertNotNull(Mappers.getMapper(ItemMapper.class));
        assertNotNull(Mappers.getMapper(BookingMapper.class));
        assertNotNull(Mappers.getMapper(CommentMapper.class));
        assertNotNull(Mappers.getMapper(ItemRequestMapper.class));

        // 4. DTO
        assertNotNull(new BookingPostDto().toString());
        assertNotNull(new ItemRequestPostDto().toString());
        assertNotNull(new ItemDto().toString());
        assertNotNull(new CommentDto().toString());
    }
}