package ru.practicum.shareit.request.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.BaseIntegrationTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestControllerTest extends BaseIntegrationTest {

    @Autowired
    private ItemRequestController itemRequestController;

    // ===========================
    // ENDPOINT: createRequest
    // ===========================

    @Test
    void shouldCreateRequestWhenUserExists() {
        UserDto requestor = userController.create(createDefaultUser().email("requestor@yandex.ru").build());
        ItemRequestPostDto postDto = new ItemRequestPostDto("Нужен перфоратор");

        ItemRequestDto createdRequest = itemRequestController.createRequest(requestor.getId(), postDto);

        assertNotNull(createdRequest.getId());
        assertEquals(postDto.getDescription(), createdRequest.getDescription());
        assertNotNull(createdRequest.getCreated());
    }

    @Test
    void shouldThrowNotFoundOnCreateWhenUserDoesNotExist() {
        ItemRequestPostDto postDto = new ItemRequestPostDto("Нужен перфоратор");

        assertThrows(NotFoundException.class, () -> itemRequestController.createRequest(999L, postDto));
    }

    // ===========================
    // ENDPOINT: findAllByRequestor
    // ===========================

    @Test
    void shouldFindAllByRequestorWithCorrectSorting() throws InterruptedException {
        UserDto requestor = userController.create(createDefaultUser().email("sorting@yandex.ru").build());

        ItemRequestDto firstRequest = itemRequestController.createRequest(requestor.getId(),
                new ItemRequestPostDto("Старый запрос"));
        Thread.sleep(10);
        ItemRequestDto secondRequest = itemRequestController.createRequest(requestor.getId(),
                new ItemRequestPostDto("Новый запрос"));

        List<ItemRequestDto> result = itemRequestController.findAllByRequestor(requestor.getId());

        assertEquals(2, result.size());
        assertEquals(secondRequest.getId(), result.get(0).getId());
        assertEquals("Новый запрос", result.get(0).getDescription());
        assertEquals(firstRequest.getId(), result.get(1).getId());
    }

    // ===========================
    // ENDPOINT: findAllRequests
    // ===========================

    @Test
    void shouldFindAllRequestsFromOtherUsersOnly() {
        UserDto user1 = userController.create(createDefaultUser().email("user1@yandex.ru").build());
        UserDto user2 = userController.create(createDefaultUser().email("user2@yandex.ru").build());

        itemRequestController.createRequest(user1.getId(), new ItemRequestPostDto("Запрос от пользователя 1"));
        ItemRequestDto requestFromUser2 = itemRequestController.createRequest(user2.getId(),
                new ItemRequestPostDto("Запрос от пользователя 2"));

        List<ItemRequestDto> result = itemRequestController.findAllRequests(user1.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals(requestFromUser2.getId(), result.getFirst().getId());
        assertEquals("Запрос от пользователя 2", result.getFirst().getDescription());
    }

    // ===========================
    // ENDPOINT: findRequest (findById)
    // ===========================

    @Test
    void shouldFindRequestByIdAndContainItemAnswers() {
        UserDto requestor = userController.create(createDefaultUser().email("requestor2@yandex.ru").build());
        UserDto itemOwner = userController.create(createDefaultUser().email("owner2@yandex.ru").build());

        ItemRequestDto requestDto = itemRequestController.createRequest(requestor.getId(),
                new ItemRequestPostDto("Ищу стремянку"));

        ItemDto itemDto = createDefaultItem()
                .name("Стремянка алюминиевая")
                .requestId(requestDto.getId())
                .build();
        ItemDto createdItem = itemController.addItem(itemOwner.getId(), itemDto);

        ItemRequestDto foundRequest = itemRequestController.findRequest(requestor.getId(), requestDto.getId());

        assertEquals(requestDto.getId(), foundRequest.getId());
        assertNotNull(foundRequest.getItems());
        assertEquals(1, foundRequest.getItems().size());
        assertEquals(createdItem.getId(), foundRequest.getItems().getFirst().getId());
        assertEquals("Стремянка алюминиевая", foundRequest.getItems().getFirst().getName());
        assertEquals(itemOwner.getId(), foundRequest.getItems().getFirst().getOwnerId());
    }

    @Test
    void shouldThrowNotFoundWhenFindNonExistentRequest() {
        UserDto user = userController.create(createDefaultUser().email("nonexist@yandex.ru").build());

        assertThrows(NotFoundException.class, () -> itemRequestController.findRequest(user.getId(), 999L));
    }
}