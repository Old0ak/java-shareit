package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Ralph Doyle")
                .email("Allen_Kilback@hotmail.com")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();
    }

    @Test
    void create_WhenUserExists_ShouldSaveRequest() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(itemRequestMapper.toItemRequest(any(ItemRequestPostDto.class))).thenReturn(itemRequest);
        Mockito.when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        Mockito.when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.create(1L, new ItemRequestPostDto("Нужна дрель"));

        assertNotNull(result);
        assertEquals("Нужна дрель", result.getDescription());
    }

    @Test
    void create_WhenUserDoesNotExist_ShouldThrowNotFound() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.create(999L,
                new ItemRequestPostDto("Текст")));
    }

    @Test
    void findAllByRequestor_WhenUserExists_ShouldReturnList() {
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(1L))
                .thenReturn(List.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(any())).thenReturn(Collections.emptyList());
        Mockito.when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class), any())).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.findAllByRequestor(1L);

        assertEquals(1, result.size());
    }

    @Test
    void findAllRequests_ShouldReturnPagedList() {
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(itemRequestRepository.findAllByRequestorIdNot(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(itemRequest)));
        Mockito.when(itemRepository.findAllByRequestIdIn(any())).thenReturn(Collections.emptyList());

        Mockito.when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class), any())).thenReturn(itemRequestDto);

        List<ItemRequestDto> result = itemRequestService.findAllRequests(1L, 0, 10);

        assertEquals(1, result.size());
    }

    @Test
    void findRequestById_WhenRequestExists_ShouldReturnDto() {
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        Mockito.when(itemRepository.findAllByRequestIdIn(List.of(1L))).thenReturn(Collections.emptyList());

        Mockito.when(itemRequestMapper.toItemRequestDto(any(ItemRequest.class), any())).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.findRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findRequestById_WhenRequestDoesNotExist_ShouldThrowNotFound() {
        Mockito.when(userRepository.existsById(1L)).thenReturn(true);
        Mockito.when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.findRequestById(1L, 999L));
    }

}
