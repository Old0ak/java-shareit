package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ServiceFinalCoverageTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void testItemServiceThrowsNotFoundWhenUserMissing() {
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.addNewItem(999L, new ItemDto()));
    }

    @Test
    void testItemServiceThrowsNotFoundWhenItemMissing() {
        Mockito.when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.findById(1L, 999L));
    }

    @Test
    void testSearchItemsWithBlankTextReturnsEmptyList() {
        assertTrue(itemService.searchItems("").isEmpty());
        assertTrue(itemService.searchItems("   ").isEmpty());
        assertTrue(itemService.searchItems(null).isEmpty());
    }

    @Test
    void testItemRequestServiceThrowsOnInvalidPagination() {
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.findAllRequests(1L, -1, 10));
        assertThrows(IllegalArgumentException.class, () ->
                itemRequestService.findAllRequests(1L, 0, 0));
    }

    @Test
    void testItemRequestServiceThrowsNotFoundWhenUserMissing() {
        Mockito.when(userRepository.existsById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class, () ->
                itemRequestService.findAllByRequestor(999L));
    }
}
