package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestPostDto itemRequestPostDto);

    List<ItemRequestDto> findAllByRequestor(Long userId);

    List<ItemRequestDto> findAllRequests(Long userId, Integer from, Integer size);

    ItemRequestDto findRequestById(Long userId, Long requestId);
}
