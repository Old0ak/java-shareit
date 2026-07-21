package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private final String headerParam = "X-Sharer-User-Id";

    // =================
    // ENDPOINT: addItem
    // =================

    @Test
    void shouldCreateItemWhenUserExists() throws Exception {
        ItemDto itemDto = ItemDto.builder().name("Дрель").description("Проводная").available(true).build();
        ItemDto savedDto = ItemDto.builder().id(1L).name("Дрель").description("Проводная").available(true).build();

        Mockito.when(itemService.addNewItem(anyLong(), any(ItemDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/items")
                        .header(headerParam, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldThrowNotFoundWhenUserDoesNotExist() throws Exception {
        ItemDto itemDto = ItemDto.builder().name("Дрель").build();

        // Исправлено: вызывается addNewItem согласно сервису
        Mockito.when(itemService.addNewItem(anyLong(), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/items")
                        .header(headerParam, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound());
    }

    // =======================
    // ENDPOINT: updateByOwner
    // =======================

    @Test
    void shouldUpdateItemCorrectly() throws Exception {
        ItemDto updateDto = ItemDto.builder().name("Новое имя").description("Новое описание").build();
        ItemDto updatedDto = ItemDto.builder().id(1L).name("Новое имя").description("Новое описание").available(true).build();

        Mockito.when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/items/1")
                        .header(headerParam, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.description").value("Новое описание"));
    }

    // =================
    // ENDPOINT: getItem
    // =================

    @Test
    void shouldFindItemById() throws Exception {
        ItemDto savedDto = ItemDto.builder().id(1L).name("Дрель").build();

        // Исправлено: вызывается findById согласно сервису
        Mockito.when(itemService.findById(anyLong(), anyLong())).thenReturn(savedDto);

        mockMvc.perform(get("/items/1")
                        .header(headerParam, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    // =============================
    // ENDPOINT: findAllItemsByOwner
    // =============================

    @Test
    void shouldFindAllItemsSuccess() throws Exception {
        ItemDto savedDto = ItemDto.builder().id(1L).name("Дрель").build();

        // Исправлено: вызывается findAll согласно сервису
        Mockito.when(itemService.findAll(anyLong())).thenReturn(List.of(savedDto));

        mockMvc.perform(get("/items")
                        .header(headerParam, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    // =====================
    // ENDPOINT: searchItems
    // =====================

    @Test
    void shouldSearchItemsByText() throws Exception {
        ItemDto savedDto = ItemDto.builder().id(1L).name("Шуруповерт").build();

        Mockito.when(itemService.searchItems(anyString())).thenReturn(List.of(savedDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "шуруп"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Шуруповерт"));
    }

    // ====================
    // ENDPOINT: addComment
    // ====================

    @Test
    void shouldCreateCommentSuccess() throws Exception {
        CommentDto inputComment = CommentDto.builder().text("Остроумный комментарий").build();
        CommentDto responseComment = CommentDto.builder()
                .id(1L)
                .text("Остроумный комментарий")
                .authorName("Booker")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(itemService.addNewComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(responseComment);

        mockMvc.perform(post("/items/1/comment")
                        .header(headerParam, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Остроумный комментарий"))
                .andExpect(jsonPath("$.authorName").value("Booker"));
    }

    @Test
    void shouldThrowExceptionWhenUserHasNotPastBooking() throws Exception {
        CommentDto inputComment = CommentDto.builder().text("Остроумный комментарий").build();

        Mockito.when(itemService.addNewComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new ConditionsNotMetException("Пользователь не может оставить отзыв"));

        mockMvc.perform(post("/items/1/comment")
                        .header(headerParam, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputComment)))
                .andExpect(status().isBadRequest());
    }
}
