package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;
    private ItemDto itemDto;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@mail.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная")
                .available(true)
                .owner(user)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная")
                .available(true)
                .build();
    }

    @Test
    void addNewItem_WhenUserExists_ShouldSaveItem() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemMapper.toItem(any())).thenReturn(item);
        Mockito.when(itemRepository.save(any())).thenReturn(item);
        Mockito.when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.addNewItem(1L, itemDto);

        assertNotNull(result);
        assertEquals("Дрель", result.getName());
    }

    @Test
    void findById_WhenItemExists_ShouldReturnDto() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Mockito.when(commentRepository.findAllByItemIdOrderByIdAsc(anyLong())).thenReturn(Collections.emptyList());

        Mockito.when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.findById(1L, 1L);

        assertNotNull(result);
    }

    @Test
    void findById_WhenItemDoesNotExist_ShouldThrowNotFound() {
        Mockito.when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(1L, 999L));
    }
}