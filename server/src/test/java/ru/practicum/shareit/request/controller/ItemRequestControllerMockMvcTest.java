package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private final String headerParam = "X-Sharer-User-Id";

    // ===========================
    // ENDPOINT: createRequest
    // ===========================

    @Test
    void shouldCreateRequestWhenUserExists() throws Exception {
        ItemRequestPostDto postDto = new ItemRequestPostDto("Нужен перфоратор");
        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен перфоратор")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(itemRequestService.create(anyLong(), any(ItemRequestPostDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(headerParam, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужен перфоратор"));
    }

    @Test
    void shouldThrowNotFoundOnCreateWhenUserDoesNotExist() throws Exception {
        ItemRequestPostDto postDto = new ItemRequestPostDto("Нужен перфоратор");

        Mockito.when(itemRequestService.create(anyLong(), any(ItemRequestPostDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(post("/requests")
                        .header(headerParam, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isNotFound());
    }

    // ===========================
    // ENDPOINT: findAllByRequestor
    // ===========================

    @Test
    void shouldFindAllByRequestor() throws Exception {
        ItemRequestDto responseDto = ItemRequestDto.builder().id(1L).description("Старый запрос").build();

        Mockito.when(itemRequestService.findAllByRequestor(anyLong())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header(headerParam, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Старый запрос"));
    }

    // ===========================
    // ENDPOINT: findAllRequests
    // ===========================

    @Test
    void shouldFindAllRequestsFromOtherUsersOnly() throws Exception {
        ItemRequestDto responseDto = ItemRequestDto.builder().id(2L).description("Запрос от пользователя 2").build();

        Mockito.when(itemRequestService.findAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header(headerParam, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    // ===========================
    // ENDPOINT: findRequest (findById)
    // ===========================

    @Test
    void shouldFindRequestById() throws Exception {
        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Ищу стремянку")
                .items(List.of())
                .build();

        Mockito.when(itemRequestService.findRequestById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header(headerParam, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Ищу стремянку"));
    }

    @Test
    void shouldThrowNotFoundWhenFindNonExistentRequest() throws Exception {
        Mockito.when(itemRequestService.findRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/999")
                        .header(headerParam, 1L))
                .andExpect(status().isNotFound());
    }
}