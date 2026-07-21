package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    // создание нового запроса
    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestBody ItemRequestPostDto itemRequestPostDto) {
        log.info("Получен запрос от пользователем: id={} на создание нового запроса на вещь", userId);
        ItemRequestDto createdRequest = itemRequestService.create(userId, itemRequestPostDto);
        log.info("Успешно создан запрос:'{}' от пользователя: id={}", createdRequest.getDescription(), userId);
        return createdRequest;
    }

    // получение пользователем списка своих запросов
    @GetMapping
    public List<ItemRequestDto> findAllByRequestor(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос от пользователя: id={} на просмотр всех своих запросов на вещи", userId);
        List<ItemRequestDto> requestsDto = itemRequestService.findAllByRequestor(userId);
        log.info("Успешно найден список всех запросов на вещи пользователя: id={}", userId);
        return requestsDto;
    }

    // получение списка запросов других пользователей
    @GetMapping("/all")
    public List<ItemRequestDto> findAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос от пользователя: id={} на список всех запросов", userId);
        List<ItemRequestDto> requestsDto = itemRequestService.findAllRequests(userId, from, size);
        log.info("Успешно найден список всех запросов");
        return requestsDto;
    }

    // получение данных о конкретном запросе
    @GetMapping("/{requestId}")
    public ItemRequestDto findRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable Long requestId) {
        log.info("Получен запрос от пользователя: id={} на получение данных о запросе по id={}", userId, requestId);
        ItemRequestDto requestDto = itemRequestService.findRequestById(userId, requestId);
        log.info("Успешно получены данные запроса id={}", requestId);
        return requestDto;
    }
}
