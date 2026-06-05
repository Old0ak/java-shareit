package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import jakarta.validation.Validator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTest {

    @Autowired
    private ItemController itemController;
    @Autowired
    private UserController userController;
    @Autowired
    private Validator validator;

    private ItemDto.ItemDtoBuilder createDefaultItem() {
        return ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true);
    }

    private UserDto createOwner(String email) {
        return userController.create(UserDto.builder()
                .email(email)
                .name("Тестовый Владелец")
                .build());
    }

    @Test
    void shouldCreateItemWhenUserExists() {
        UserDto owner = createOwner("owner@yandex.ru");
        ItemDto itemDto = createDefaultItem().build();

        ItemDto createdItem = itemController.add(owner.getId(), itemDto);

        assertNotNull(createdItem.getId());
        assertEquals(itemDto.getName(), createdItem.getName());
        assertTrue(createdItem.getAvailable());
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() {
        ItemDto itemDto = createDefaultItem().build();

        assertThrows(NotFoundException.class, () -> itemController.add(999L, itemDto));
    }

    @Test
    void shouldUpdateItemCorrectly() {
        UserDto owner = createOwner("update@yandex.ru");
        ItemDto createdItem = itemController.add(owner.getId(), createDefaultItem().build());

        ItemDto updateDto = ItemDto.builder().name("Новое имя").description("Новое описание").build();
        ItemDto updatedItem = itemController.updateByOwner(owner.getId(), createdItem.getId(), updateDto);

        assertEquals("Новое имя", updatedItem.getName());
        assertEquals("Новое описание", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
    }

    @Test
    void shouldFindItemById() {
        UserDto owner = createOwner("find@yandex.ru");
        ItemDto createdItem = itemController.add(owner.getId(), createDefaultItem().build());

        ItemDto foundItem = itemController.getItem(createdItem.getId());

        assertEquals(createdItem, foundItem);
    }

    @Test
    void shouldFindAllItemsByOwnerWithoutOrdering() {
        UserDto owner = createOwner("all@yandex.ru");
        ItemDto item1 = itemController.add(owner.getId(), createDefaultItem().name("Вещь 1").build());
        ItemDto item2 = itemController.add(owner.getId(), createDefaultItem().name("Вещь 2").build());

        List<ItemDto> expectedItems = List.of(item1, item2);
        List<ItemDto> actualItems = itemController.findAllItemsByOwner(owner.getId());

        assertEquals(expectedItems.size(), actualItems.size());
        assertThat(actualItems).containsExactlyInAnyOrderElementsOf(expectedItems);
    }

    @Test
    void shouldSearchItemsByTextWithoutOrdering() {
        UserDto owner = createOwner("search@yandex.ru");
        ItemDto item1 = itemController.add(owner.getId(), createDefaultItem().name("Шуруповерт").build());
        ItemDto item2 = itemController.add(owner.getId(), createDefaultItem().name("Дрель-шуруповерт").build());

        itemController.add(owner.getId(), createDefaultItem().name("Молоток").build());

        List<ItemDto> expectedSearch = List.of(item1, item2);
        List<ItemDto> actualSearch = itemController.searchItems("шуруп");

        assertEquals(expectedSearch.size(), actualSearch.size());
        assertThat(actualSearch).containsExactlyInAnyOrderElementsOf(expectedSearch);
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsBlank() {
        UserDto owner = createOwner("blank@yandex.ru");
        itemController.add(owner.getId(), createDefaultItem().build());

        List<ItemDto> result = itemController.searchItems("   ");

        assertTrue(result.isEmpty());
    }
}
