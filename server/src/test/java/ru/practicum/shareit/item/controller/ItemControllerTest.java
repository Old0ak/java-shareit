package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.BaseIntegrationTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ItemControllerTest extends BaseIntegrationTest {

    // =================
    // ENDPOINT: addItem
    // =================

    @Test
    void shouldCreateItemWhenUserExists() {
        UserDto owner = userController.create(createDefaultUser().email("owner@yandex.ru").build());
        ItemDto itemDto = createDefaultItem().build();

        ItemDto createdItem = itemController.addItem(owner.getId(), itemDto);

        assertNotNull(createdItem.getId());
        assertEquals(itemDto.getName(), createdItem.getName());
        assertTrue(createdItem.getAvailable());
    }

    @Test
    void shouldCreateItemWithRequestIdSuccessfully() {
        UserDto owner = userController.create(createDefaultUser().email("item_owner@yandex.ru").build());
        UserDto requestor = userController.create(createDefaultUser().email("item_req@yandex.ru").build());

        ru.practicum.shareit.request.dto.ItemRequestPostDto postDto =
                new ru.practicum.shareit.request.dto.ItemRequestPostDto("Нужен кабель");
        ru.practicum.shareit.request.dto.ItemRequestDto request =
                itemRequestController.createRequest(requestor.getId(), postDto);

        ItemDto itemDto = createDefaultItem().requestId(request.getId()).build();
        ItemDto createdItem = itemController.addItem(owner.getId(), itemDto);

        assertNotNull(createdItem.getId());
        assertEquals(request.getId(), createdItem.getRequestId());
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        ItemDto itemDto = createDefaultItem().build();

        assertThrows(NotFoundException.class, () -> itemController.addItem(999L, itemDto));
    }

    // =======================
    // ENDPOINT: updateByOwner
    // =======================

    @Test
    void shouldUpdateItemCorrectly() {
        UserDto owner = userController.create(createDefaultUser().email("update@yandex.ru").build());
        ItemDto createdItem = itemController.addItem(owner.getId(), createDefaultItem().build());

        ItemDto updateDto = ItemDto.builder().name("Новое имя").description("Новое описание").build();
        ItemDto updatedItem = itemController.updateByOwner(owner.getId(), createdItem.getId(), updateDto);

        assertEquals("Новое имя", updatedItem.getName());
        assertEquals("Новое описание", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
    }

    // =================
    // ENDPOINT: getItem
    // =================

    @Test
    void shouldFindItemById() {
        UserDto owner = userController.create(createDefaultUser().email("find@yandex.ru").build());
        ItemDto createdItem = itemController.addItem(owner.getId(), createDefaultItem().build());

        ItemDto foundItem = itemController.getItem(owner.getId(), createdItem.getId());

        assertEquals(createdItem.getId(), foundItem.getId());
        assertEquals(createdItem.getName(), foundItem.getName());
    }

    // =============================
    // ENDPOINT: findAllItemsByOwner
    // =============================

    @Test
    void shouldFindAllItemsSuccess() {
        UserDto owner = userController.create(createDefaultUser().email("find@yandex.ru").build());
        ItemDto createdItem1 = itemController.addItem(owner.getId(), createDefaultItem().build());
        ItemDto createdItem2 = itemController.addItem(owner.getId(), createDefaultItem()
                .name("Кирпич")
                .description("Если взять 4, то можно поставить на них машину без колес")
                .build());

        List<ItemDto> foundItems = itemController.findAllItemsByOwner(owner.getId());

        assertEquals(2, foundItems.size());

        assertEquals(createdItem1.getId(), foundItems.getFirst().getId());
        assertEquals(createdItem1.getName(), foundItems.getFirst().getName());

        assertEquals(createdItem2.getId(), foundItems.get(1).getId());
        assertEquals(createdItem2.getName(), foundItems.get(1).getName());
    }

    @Test
    void shouldFindAllItemsByOwnerWithoutOrdering() {
        UserDto owner = userController.create(createDefaultUser().email("all@yandex.ru").build());
        ItemDto item1 = itemController.addItem(owner.getId(), createDefaultItem().name("Вещь 1").build());
        ItemDto item2 = itemController.addItem(owner.getId(), createDefaultItem().name("Вещь 2").build());

        ItemDto expectedItem1 = item1.toBuilder().comments(List.of()).build();
        ItemDto expectedItem2 = item2.toBuilder().comments(List.of()).build();

        List<ItemDto> expectedItems = List.of(expectedItem1, expectedItem2);
        List<ItemDto> actualItems = itemController.findAllItemsByOwner(owner.getId());

        assertEquals(expectedItems.size(), actualItems.size());
        assertThat(actualItems).containsExactlyInAnyOrderElementsOf(expectedItems);
    }

    // =====================
    // ENDPOINT: searchItems
    // =====================

    @Test
    void shouldSearchItemsByTextWithoutOrdering() {
        UserDto owner = userController.create(createDefaultUser().email("search@yandex.ru").build());
        ItemDto item1 = itemController.addItem(owner.getId(), createDefaultItem().name("Шуруповерт").build());
        ItemDto item2 = itemController.addItem(owner.getId(), createDefaultItem().name("Дрель-шуруповерт").build());

        itemController.addItem(owner.getId(), createDefaultItem().name("Молоток").build());

        List<ItemDto> expectedSearch = List.of(item1, item2);
        List<ItemDto> actualSearch = itemController.searchItems("шуруп");

        assertEquals(expectedSearch.size(), actualSearch.size());
        assertThat(actualSearch).containsExactlyInAnyOrderElementsOf(expectedSearch);
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsBlank() {
        UserDto owner = userController.create(createDefaultUser().email("blank@yandex.ru").build());
        itemController.addItem(owner.getId(), createDefaultItem().build());

        List<ItemDto> result = itemController.searchItems("   ");

        assertTrue(result.isEmpty());
    }

    // ====================
    // ENDPOINT: addComment
    // ====================

    @Test
    void shouldCreateCommentSuccess() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto pastBooking = bookingController.add(booker.getId(), postDto);

        bookingController.approve(owner.getId(), pastBooking.getId(), true);

        Booking pastBookingEntity = bookingRepository.findById(pastBooking.getId()).orElseThrow();
        pastBookingEntity.setStart(LocalDateTime.now().minusHours(2));
        pastBookingEntity.setEnd(LocalDateTime.now().minusHours(1));
        bookingRepository.saveAndFlush(pastBookingEntity);

        CommentDto result = itemController.addComment(booker.getId(), item.getId(), createDefaultComment().build());

        assertNotNull(result.getId());
        assertEquals("Остроумный комментарий", result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
    }

    @Test
    void shouldThrowExceptionWhenUserHasNotPastBooking() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto futureBooking = bookingController.add(booker.getId(), postDto);

        bookingController.approve(owner.getId(), futureBooking.getId(), true);

        assertThrows(ConditionsNotMetException.class, () -> itemController.addComment(
                booker.getId(), item.getId(), createDefaultComment().build()));
    }
}
