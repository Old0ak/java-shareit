package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    // добавление пользователем вещи
    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        ItemDto addedItem = itemService.addNewItem(userId, itemDto);
        log.info("Пользователь id={} добавил новую вещь: id={}, name={}",
                userId, addedItem.getId(), addedItem.getName()
        );
        return addedItem;
    }

    // обновление владельцем данных о вещи
    @PatchMapping("/{itemId}")
    public ItemDto updateByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        log.info("Владелец id={} обновил данные о вещи: id={}. Новые данные: {}", userId, itemId, updatedItem);
        return updatedItem;
    }

    // получение любым пользователем данных о вещи
    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        ItemDto itemDto = itemService.findById(itemId);
        log.info("Найдена информация о вещи id={}", itemId);
        return itemDto;
    }

    // получение владельцем данных о всех своих вещах
    @GetMapping
    public List<ItemDto> findAllItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = itemService.findAll(userId);
        log.info("Владелец id={} получил список всех своих вещей", userId);
        return items;
    }

    // поиск вещей пользователем для аренды
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        List<ItemDto> items = itemService.searchItems(text);
        log.info("Найдены вещи по запросу text={}", text);
        return items;
    }
}
