package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.storage.BookingStatus;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto addNewItem(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        validateCreateItem(itemDto);

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            ItemRequest request = getItemRequestOrThrow(requestId);
            item.setRequest(request);
        }

        log.debug("Добавление владельцем ownerId={} новой вещи name={}", userId, item.getName());
        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItemOrThrow(itemId);

        validateOwner(item.getOwner().getId(), userId);

        Optional.ofNullable(itemDto.getName())
                .filter(name -> !name.isBlank())
                .ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription())
                .filter(description -> !description.isBlank())
                .ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable())
                .ifPresent(item::setAvailable);

        log.debug("Обновление владельцем ownerId={} данных о вещи itemId={}", userId, itemId);
        Item updatedItem = itemRepository.save(item);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        log.debug("Получение пользователем с id={} вещи по id={}", userId, itemId);
        Item item = getItemOrThrow(itemId);
        return enrichWithBookings(item, userId);
    }

    @Override
    public List<ItemDto> findAll(Long userId) {
        validateUserId(userId);

        log.debug("Получение владельцем userId={} списка своих вещей", userId);
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(item -> enrichWithBookings(item, userId))
                .sorted(Comparator.comparing(ItemDto::getId))
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        log.debug("Поиск вещей по запросу text={}", text);
        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addNewComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        LocalDateTime now = LocalDateTime.now();

        validateHasPastBooking(userId, itemId, now);

        Comment comment = commentMapper.toComment(commentDto);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(now);

        log.debug("Добавление комментария:{} от автора бронирования:id={} для вещи id={}",
                comment.getText(), userId, itemId);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(savedComment);
    }

    private ItemDto.BookingShortDto toShortBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new ItemDto.BookingShortDto(booking.getId(), booking.getBooker().getId());
    }

    private ItemDto enrichWithBookings(Item item, Long userId) {
        ItemDto dto = itemMapper.toItemDto(item);

        List<CommentDto> comments = commentRepository.findAllByItemIdOrderByIdAsc(item.getId()).stream()
                .map(commentMapper::toCommentDto)
                .toList();

        dto = dto.toBuilder().comments(comments).build();

        if (item.getOwner().getId().equals(userId)) {
            log.debug("Обогащение запроса для владельца id={} вещи id{} " +
                            "информацией о последним и следующим бронированием",
                    userId, item.getId());
            LocalDateTime now = LocalDateTime.now();

            Booking last = bookingRepository.findAllByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .stream()
                    .findFirst()
                    .orElse(null);

            Booking next = bookingRepository.findAllByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            item.getId(), BookingStatus.APPROVED, now)
                    .stream()
                    .findFirst()
                    .orElse(null);

            dto = dto.toBuilder()
                    .lastBooking(toShortBookingDto(last))
                    .nextBooking(toShortBookingDto(next))
                    .build();
        }
        return dto;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id = " + itemId + " не найдена"));
    }

    private ItemRequest getItemRequestOrThrow(Long requestId) {
        return itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с id = " + requestId + " не найден"));
    }

    private void validateUserId(Long userId) {
        if (!userRepository.existsById(userId))
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
    }

    private void validateCreateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ConditionsNotMetException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ConditionsNotMetException("Статус доступности вещи обязателен к заполнению");
        }
    }

    private void validateOwner(Long ownerId, Long userId) {
        if (!ownerId.equals(userId))
            throw new NotFoundException("Пользователь c id = " + userId + " не является владельцем");
    }

    private void validateHasPastBooking(Long userId, Long itemId, LocalDateTime now) {
        if (!bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, now)) {
            throw new ConditionsNotMetException("Оставить отзыв может только пользователь," +
                    " который арендовал эту вещь, и чья аренда уже завершилась");
        }
    }
}
