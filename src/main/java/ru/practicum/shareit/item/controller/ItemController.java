package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
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
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на добавление новой вещи пользователем: id={}", userId);
        ItemDto addedItem = itemService.addNewItem(userId, itemDto);
        log.info("Пользователь id={} успешно добавил новую вещь: id={}, name={}",
                userId, addedItem.getId(), addedItem.getName()
        );
        return addedItem;
    }

    // обновление владельцем данных о вещи
    @PatchMapping("/{itemId}")
    public ItemDto updateByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на обновление данных пользователем: id={} о вещи: id={}", userId, itemId);
        ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        log.info("Владелец id={} успешно обновил данные о вещи: id={}. Новые данные: {}", userId, itemId, updatedItem);
        return updatedItem;
    }

    // получение любым пользователем данных о вещи
    @GetMapping("/{itemId}")
    public ItemDto getItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long itemId) {
        log.info("Получен запрос от пользователя: id={} на получение информации о вещи id={}", userId, itemId);
        ItemDto itemDto = itemService.findById(userId, itemId);
        log.info("Успешно найдена информация для пользователя: id={} о вещи id={}", userId, itemId);
        return itemDto;
    }

    // получение владельцем данных о всех своих вещах
    @GetMapping
    public List<ItemDto> findAllItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос от пользователя: id={} на получение списка всех своих вещей", userId);
        List<ItemDto> items = itemService.findAll(userId);
        log.info("Владелец id={} успешно получил список всех своих вещей", userId);
        return items;
    }

    // поиск вещей пользователем для аренды
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Получен запрос на поиск вещей по запросу text={}", text);
        List<ItemDto> items = itemService.searchItems(text);
        log.info("Успешно найдены вещи по запросу text={}", text);
        return items;
    }

    // добавление комментария
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentDto commentDto) {
        log.info("Получен запрос от пользователя: id={} на добавление комментария к вещи id={}", userId, itemId);
        CommentDto addedComment = itemService.addNewComment(userId, itemId, commentDto);
        log.info("Успешно добавлен комментарий:'{}' от автора бронирования:id={} для вещи id={}",
                addedComment.getText(), userId, itemId);
        return addedComment;
    }
}
