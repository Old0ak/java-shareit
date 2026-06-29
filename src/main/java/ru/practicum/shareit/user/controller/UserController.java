package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.create(userDto);
        log.info("Добавлен пользователь: id={}, email={}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
        log.info("Удалён пользователь: id={}", userId);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.update(userId, userDto);
        log.info("Обновлены данные пользователя: id={}", updatedUser.getId());
        return updatedUser;
    }

    @GetMapping
    public List<UserDto> findAll() {
        List<UserDto> users = userService.findAll();
        log.info("Получен список всех пользователей");
        return users;
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        UserDto userDto = userService.findById(userId);
        log.info("Получен пользователь: id={}", userId);
        return userDto;
    }
}
