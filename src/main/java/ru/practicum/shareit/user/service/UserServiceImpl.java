package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final UserMapper mapper;

    public UserDto create(UserDto userDto) {
        validateForCreate(userDto);

        checkEmailDuplicate(userDto.getEmail(), null);

        User user = mapper.toUser(userDto);
        log.debug("Создание пользователя: id={}", user.getId());
        return mapper.toUserDto(userStorage.create(user));
    }

    public void delete(Long userId) {
        validateId(userId);

        log.debug("Удаление пользователя: id={}", userId);
        userStorage.delete(userId);
    }

    public UserDto update(Long userId, UserDto userDto) {
        User user = getUserOrThrow(userId);

        Optional.ofNullable(userDto.getName())
                .filter(name -> !name.isBlank())
                .ifPresent(user::setName);

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            checkEmailDuplicate(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }

        log.debug("Обновление данных пользователя: id={}", user.getId());
        User updatedUser = userStorage.update(user);
        return mapper.toUserDto(updatedUser);
    }

    public List<UserDto> findAll() {
        log.debug("Получение списка всех пользователей");
        return userStorage.findAll().stream()
                .map(mapper::toUserDto)
                .toList();
    }

    public UserDto findById(Long userId) {
        log.debug("Получение пользователя по id={}", userId);
        User user = getUserOrThrow(userId);
        return mapper.toUserDto(user);
    }

    private void checkEmailDuplicate(String email, Long excludeUserId) {
        boolean exists = userStorage.findAll().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email)
                        && !java.util.Objects.equals(user.getId(), excludeUserId));

        if (exists) {
            throw new ConflictException("Пользователь с email=" + email + " уже существует");
        }
    }

    private void validateForCreate(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank())
            throw new ConditionsNotMetException("Email не может быть пустым");

        if (userDto.getName() == null || userDto.getName().isBlank())
            throw new ConditionsNotMetException("Имя не может быть пустым");
    }

    private void validateId(Long userId) {
        if (!userStorage.existsById(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + "не найден"));
    }
}
