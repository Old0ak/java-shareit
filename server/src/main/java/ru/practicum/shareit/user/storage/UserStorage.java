package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    void delete(Long userId);

    User update(User user);

    Collection<User> findAll();

    Optional<User> findById(Long userId);

    boolean existsById(Long userId);
}
