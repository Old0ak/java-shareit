package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final ItemMapper mapper;

    @Override
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        validateCreateItem(itemDto);

        Item item = mapper.toItem(itemDto);
        item.setOwner(owner);
        log.debug("Добавление владельцем ownerId={} новой вещи name={}",
               userId, item.getName());
        return mapper.toItemDto(itemStorage.addNewItem(item));
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItemOrThrow(itemId);

        validateOwner(item.getOwner().getId(), userId);

        Optional.ofNullable(itemDto.getName())
                .filter(name -> !name.isBlank())
                .ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription())
                .filter(description -> !description.isBlank())
                .ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable())
                .ifPresent(item::setAvailable);

        log.debug("Обновление владельцем ownerId={} данных о вещи itemId={}", userId, itemId);
        Item updatedItem = itemStorage.update(item);
        return mapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long itemId) {
        log.debug("Получение вещи по id={}", itemId);
        Item item = getItemOrThrow(itemId);
        return mapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findAll(Long userId) {
        validateUserId(userId);

        log.debug("Получение владельцем userId={} списка своих вещей", userId);
        return itemStorage.findAllByOwner(userId).stream()
                .map(mapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        log.debug("Поиск вещей по запросу text={}", text);
        return itemStorage.searchItems(text).stream()
                .map(mapper::toItemDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemStorage.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id = " + itemId + " не найдена"));
    }

    private void validateUserId(Long userId) {
        if (!userStorage.existsById(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
    }

    private void validateCreateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ConditionsNotMetException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ConditionsNotMetException("Статус доступности вещи обязателен к заполнению");
        }
    }

    private void validateOwner(Long ownerId, Long userId) {
        if (!ownerId.equals(userId))
            throw new NotFoundException("Пользователь c id = " + userId + " не является владельцем");
    }
}
