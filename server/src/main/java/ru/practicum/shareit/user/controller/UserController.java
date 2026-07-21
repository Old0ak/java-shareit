package ru.practicum.shareit.user.controller;

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

    // создание нового пользователя
    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Получен запрос на создание нового пользователя");
        UserDto createdUser = userService.create(userDto);
        log.info("Успешно создан пользователь: id={}, email={}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    // удаление пользователя
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("Получен запрос на удаление пользователя: id={}", userId);
        userService.delete(userId);
        log.info("Успешно удалён пользователь: id={}", userId);
    }

    // обновление данных пользователя
    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId,
                          @RequestBody UserDto userDto) {
        log.info("Получен запрос на обновление данных пользователя: id={}", userId);
        UserDto updatedUser = userService.update(userId, userDto);
        log.info("Успешно обновлены данные пользователя: id={}", updatedUser.getId());
        return updatedUser;
    }

    // получение списка всех пользователей
    @GetMapping
    public List<UserDto> findAll() {
        log.info("Получен запрос на получение списка всех пользователей");
        List<UserDto> users = userService.findAll();
        log.info("Успешно получен список всех пользователей");
        return users;
    }

    // получение пользователя по id
    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Получен запрос на получение пользователя: id={}", userId);
        UserDto userDto = userService.findById(userId);
        log.info("Успешно получен пользователь: id={}", userId);
        return userDto;
    }
}
