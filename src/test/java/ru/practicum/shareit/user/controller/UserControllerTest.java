package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import jakarta.validation.Validator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private UserController userController;
    @Autowired
    private Validator validator;

    private UserDto.UserDtoBuilder createDefaultUser() {
        return UserDto.builder()
                .email("test@yandex.ru")
                .name("Тестов Тест Тестович");
    }

    @Test
    void shouldCreateUserWhenDataIsValid() {
        UserDto user = createDefaultUser().build();

        UserDto createdUser = userController.create(user);

        assertNotNull(createdUser.getId());
        assertEquals(user.getEmail(), createdUser.getEmail());
        assertEquals(user.getName(), createdUser.getName());
    }

    @Test
    void shouldFallsValidateWhenEmailIsBlank() {
        UserDto user = createDefaultUser().email(" ").build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFallsValidateWhenEmailWithoutAt() {
        UserDto user = createDefaultUser().email("testyandex.ru").build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFallsValidateWhenNameIsBlank() {
        UserDto user = createDefaultUser().name(" ").build();

        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldDeleteUser() {
        UserDto user = createDefaultUser().build();
        UserDto createdUser = userController.create(user);

        assertDoesNotThrow(() -> userController.delete(createdUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenDeleteNonExistentUser() {
        assertThrows(NotFoundException.class,() -> userController.delete(999L));
    }

    @Test
    void shouldFindAllUsers() {
        UserDto user1 = createDefaultUser().build();
        UserDto user2 = createDefaultUser()
                .email("2variant@yandex.ru")
                .name("Вариантов Вариант Вариантович")
                .build();

        List<UserDto> users = List.of(
                userController.create(user1),
                userController.create(user2)
        );

        List<UserDto> createdUsers = userController.findAll();

        assertEquals(users.size(), createdUsers.size());
        assertTrue(createdUsers.containsAll(users));
    }

    @Test
    void shouldFindUserByValidId() {
        UserDto user = createDefaultUser().build();
        UserDto expectedUser = userController.create(user);
        UserDto actualUser = userController.getUser(expectedUser.getId());

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void shouldThrowExceptionWhenFindNonExistentUser() {
        assertThrows(NotFoundException.class,() -> userController.getUser(999L));
    }
}
