package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new LinkedHashMap<>();

    private Long generatedId = 0L;

    @Override
    public Item addNewItem(Item item) {
        item.setId(++generatedId);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Collection<Item> findAllByOwner(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public Collection<Item> searchItems(String text) {
        String query = text.toLowerCase();

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> isMatch(item.getName(), query) || isMatch(item.getDescription(), query))
                .toList();
    }

    private boolean isMatch(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }
}
