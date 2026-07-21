package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    Item addNewItem(Item item);

    Item update(Item item);

    Optional<Item> findById(Long itemId);

    Collection<Item> findAllByOwner(Long userId);

    Collection<Item> searchItems(String text);
}
