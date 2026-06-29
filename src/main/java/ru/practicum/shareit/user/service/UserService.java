package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserDto userDto);

    void delete(Long userId);

    UserDto update(Long userId, UserDto userDto);

    List<UserDto> findAll();

    UserDto findById(Long userId);
}
