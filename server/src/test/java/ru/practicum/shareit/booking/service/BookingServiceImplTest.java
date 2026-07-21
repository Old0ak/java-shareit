package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void findById_WhenBookingDoesNotExist_ShouldThrowNotFound() {
        Mockito.when(userRepository.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findById(1L, 999L));
    }

    @Test
    void findAllByState_WhenAll_ShouldReturnList() {
        Mockito.when(userRepository.existsById(anyLong())).thenReturn(true);

        Mockito.when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong()))
                .thenReturn(Collections.emptyList());

        List<BookingDto> result = bookingService.findAllByState(1L, ru.practicum.shareit.booking.BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByOwner_WhenAll_ShouldReturnList() {
        Mockito.when(userRepository.existsById(anyLong())).thenReturn(true);

        Item mockItem = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Проводная дрель")
                .available(true)
                .build();

        Mockito.when(itemRepository.findAllByOwnerId(anyLong())).thenReturn(List.of(mockItem));

        Mockito.when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(anyLong()))
                .thenReturn(Collections.emptyList());

        List<BookingDto> result = bookingService.findAllByOwner(1L, ru.practicum.shareit.booking.BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
