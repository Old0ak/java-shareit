package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ru.practicum.shareit.user.mapper.UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("User").email("user@mail.ru").build();
        userDto = UserDto.builder().id(1L).name("User").email("user@mail.ru").build();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertNotNull(result);
        assertEquals("user@mail.ru", result.getEmail());
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldThrowNotFound() {
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void findAll_ShouldReturnList() {
        Mockito.when(userRepository.findAll()).thenReturn(List.of(user));
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void update_WhenUserExists_ShouldUpdateFields() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(userRepository.save(any())).thenReturn(user);
        Mockito.when(userMapper.toUserDto(any(User.class))).thenReturn(userDto);

        UserDto updateDto = UserDto.builder().name("NewName").build();
        UserDto result = userService.update(1L, updateDto);

        assertNotNull(result);
    }
}
