package ru.practicum.shareit;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // Контроллеры
    @Autowired
    protected UserController userController;
    @Autowired
    protected ItemController itemController;
    @Autowired
    protected BookingController bookingController;
    @Autowired
    protected ItemRequestController itemRequestController;

    // Репозитории
    @Autowired
    protected BookingRepository bookingRepository;

    // Validator
    @Autowired
    protected Validator validator;

    // Создание базовых Dto
    protected UserDto.UserDtoBuilder createDefaultUser() {
        return UserDto.builder()
                .email("test@yandex.ru")
                .name("Тестов Тест Тестович");
    }

    protected ItemDto.ItemDtoBuilder createDefaultItem() {
        return ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true);
    }

    protected BookingPostDto.BookingPostDtoBuilder createDefaultBooking(Long itemId) {
        return BookingPostDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2));
    }

    protected CommentDto.CommentDtoBuilder createDefaultComment() {
        return CommentDto.builder()
                .text("Остроумный комментарий");
    }

}
