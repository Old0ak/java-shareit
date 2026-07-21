package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper mapper;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestPostDto itemRequestPostDto) {
        User requestor = getUserOrThrow(userId);

        ItemRequest itemRequest = mapper.toItemRequest(itemRequestPostDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        log.debug("Создание запроса вещи:'{}' от пользователя: id={}", itemRequest.getDescription(), userId);
        ItemRequest createdItemRequest = itemRequestRepository.save(itemRequest);
        return mapper.toItemRequestDto(createdItemRequest);
    }

    @Override
    public List<ItemRequestDto> findAllByRequestor(Long userId) {
        validateUserId(userId);

        log.debug("Получение всех запросов пользователя: id={}", userId);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> findAllRequests(Long userId, Integer from, Integer size) {
        validateFromAndSize(from, size);
        validateUserId(userId);

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());
        log.debug("Получение всех запросов");
        Page<ItemRequest> requestsPage = itemRequestRepository.findAllByRequestorIdNot(userId, pageable);
        List<ItemRequest> requests = requestsPage.getContent();
        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto findRequestById(Long userId, Long requestId) {
        validateUserId(userId);

        log.debug("Получение запроса по id = {}", requestId);
        ItemRequest itemRequest = getItemRequestOrThrow(requestId);
        List<Item> items = itemRepository.findAllByRequestIdIn(List.of(requestId));
        return mapper.toItemRequestDto(itemRequest, items);
    }

    private List<ItemRequestDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }
        log.debug("Найдено {} запросов", requests.size());

        log.debug("Получение id всех запросов");
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        log.debug("Получение списка вещей на основе всех id запросов");
        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);
        log.debug("Загружено {} вещей-ответов для запросов с id: {}", items.size(), requestIds);

        log.debug("Группировка вещей по id запроса");
        Map<Long, List<Item>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        log.debug("Формирование списка всех запросов на вещи");
        return requests.stream()
                .map(request -> {
                    List<Item> itemsForRequest = itemsByRequestId.getOrDefault(request.getId(), new ArrayList<>());
                    return mapper.toItemRequestDto(request, itemsForRequest);
                })
                .toList();
    }

    private void validateFromAndSize(Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Некорректные параметры пагинации");
        }
    }

    private void validateUserId(Long userId) {
        if (!userRepository.existsById(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private ItemRequest getItemRequestOrThrow(Long requestId) {
        return itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с id = " + requestId + " не найден"));
    }

}
