package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // ================
    // ENDPOINT: create
    // ================

    @Test
    void shouldCreateUserWhenDataIsValid() throws Exception {
        UserDto userDto = UserDto.builder().name("User").email("user@yandex.ru").build();
        UserDto savedDto = UserDto.builder().id(1L).name("User").email("user@yandex.ru").build();

        Mockito.when(userService.create(any(UserDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@yandex.ru"));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        UserDto userDto = UserDto.builder().name("User").email(" ").build();

        Mockito.when(userService.create(any(UserDto.class)))
                .thenThrow(new ConditionsNotMetException("Email не может быть пустым"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    // ================
    // ENDPOINT: delete
    // ================

    @Test
    void shouldDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).delete(anyLong());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistentUser() throws Exception {
        Mockito.doThrow(new NotFoundException("Пользователь не найден")).when(userService).delete(anyLong());

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound());
    }

    // =================
    // ENDPOINT: findAll
    // =================

    @Test
    void shouldFindAllUsers() throws Exception {
        UserDto user = UserDto.builder().id(1L).name("User").email("user@yandex.ru").build();
        Mockito.when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // =================
    // ENDPOINT: getUser
    // =================

    @Test
    void shouldFindUserByValidId() throws Exception {
        UserDto user = UserDto.builder().id(1L).name("User").email("user@yandex.ru").build();
        Mockito.when(userService.findById(anyLong())).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ================
    // ENDPOINT: update
    // ================

    @Test
    void shouldUpdateOnlyChangeFields() throws Exception {
        UserDto updateDto = UserDto.builder().name("Новое Имя").build();
        UserDto updatedDto = UserDto.builder().id(1L).name("Новое Имя").email("user@yandex.ru").build();

        Mockito.when(userService.update(anyLong(), any(UserDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое Имя"))
                .andExpect(jsonPath("$.email").value("user@yandex.ru"));
    }
}
