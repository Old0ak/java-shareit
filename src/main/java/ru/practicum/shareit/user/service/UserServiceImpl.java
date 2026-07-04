package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        validateForCreate(userDto);
        checkEmailDuplicate(userDto.getEmail(), null);

        User user = mapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        log.debug("Создание пользователя: id={}", savedUser.getId());
        return mapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        validateId(userId);

        log.debug("Удаление пользователя: id={}", userId);
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
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
        User updatedUser = userRepository.save(user);
        return mapper.toUserDto(updatedUser);
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("Получение списка всех пользователей");
        return userRepository.findAll().stream()
                .map(mapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto findById(Long userId) {
        log.debug("Получение пользователя по id={}", userId);
        User user = getUserOrThrow(userId);
        return mapper.toUserDto(user);
    }

    private void checkEmailDuplicate(String email, Long excludeUserId) {
        boolean exists;

        if (excludeUserId == null) {
            exists = userRepository.existsByEmailIgnoreCase(email);
        } else {
            exists = userRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeUserId);
        }

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
        if (!userRepository.existsById(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}
