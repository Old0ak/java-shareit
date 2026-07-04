package ru.practicum.shareit.booking.controller;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.BaseIntegrationTest;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingPostDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStatus;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingControllerTest extends BaseIntegrationTest {

    // =============
    // ENDPOINT: add
    // =============

    @Test
    void shouldCreateBookingSuccess() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());

        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto result = bookingController.add(booker.getId(), postDto);

        assertNotNull(result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
    }

    @Test
    void shouldThrowExceptionWhenIncorrectDate() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());

        BookingPostDto postDtoWithIncorrectDate = createDefaultBooking(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().minusHours(1))
                .build();

        assertThrows(ConditionsNotMetException.class, () -> bookingController.add(
                booker.getId(), postDtoWithIncorrectDate));
    }

    @Test
    void shouldThrowExceptionWhenAvailableIsFalse() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().available(false).build());

        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        assertThrows(ConditionsNotMetException.class, () -> bookingController.add(booker.getId(), postDto));
    }

    @Test
    void shouldThrowExceptionWhenIsOwner() {
        UserDto owner = userController.create(createDefaultUser().build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());

        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        assertThrows(NotFoundException.class, () -> bookingController.add(owner.getId(), postDto));
    }

    // =================
    // ENDPOINT: approve
    // =================

    @Test
    void shouldApproveBookingSuccess() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);
        BookingDto result = bookingController.approve(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
        assertEquals(booking.getId(), result.getId());
        assertNotEquals(booking.getStatus(), result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenIsNotOwner() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto notOwner = userController.create(createDefaultUser().email("notowner@yandex.ru)").build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);

        assertThrows(ConditionsNotMetException.class, () -> bookingController.approve(
                notOwner.getId(), booking.getId(), true));
    }

    @Test
    void shouldThrowExceptionWhenStatusAlreadyApproved() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);
        BookingDto bookingApproved = bookingController.approve(owner.getId(), booking.getId(), true);

        assertThrows(ConditionsNotMetException.class, () -> bookingController.approve(
                owner.getId(), bookingApproved.getId(), true));
    }

    // ====================
    // ENDPOINT: getBooking
    // ====================

    @Test
    void shouldBookingFindByIdByOwnerSuccess() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);
        BookingDto result = bookingController.getBooking(owner.getId(), booking.getId());

        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStatus(), result.getStatus());
        assertEquals(booking.getItem().getId(), result.getItem().getId());
    }

    @Test
    void shouldBookingFindByIdByBookerSuccess() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);
        BookingDto result = bookingController.getBooking(booker.getId(), booking.getId());

        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStatus(), result.getStatus());
        assertEquals(booking.getItem().getId(), result.getItem().getId());
    }

    @Test
    void shouldThrowExceptionWhenFindByIdByNotOwnerAndBooker() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        UserDto notOwnerAndBooker = userController.create(
                createDefaultUser().email("notownerandbooker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);

        assertThrows(ConditionsNotMetException.class, () -> bookingController.getBooking(
                notOwnerAndBooker.getId(), booking.getId()));
    }

    // ===============================
    // ENDPOINT: findAllBookingsByUser
    // ===============================

    @Test
    void shouldFindAllBookingsByUserWithStateCurrent() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto currentBooking = bookingController.add(booker.getId(), postDto);

        Booking currentBookingEntity = bookingRepository.findById(currentBooking.getId()).orElseThrow();
        currentBookingEntity.setStart(LocalDateTime.now().minusHours(1));
        currentBookingEntity.setEnd(LocalDateTime.now().plusHours(1));
        bookingRepository.saveAndFlush(currentBookingEntity);

        List<BookingDto> result = bookingController.findAllBookingsByUser(booker.getId(), BookingState.CURRENT);

        assertEquals(1, result.size());
        assertEquals(currentBooking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByUserWithStatePast() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto pastBooking = bookingController.add(booker.getId(), postDto);

        Booking pastBookingEntity = bookingRepository.findById(pastBooking.getId()).orElseThrow();
        pastBookingEntity.setStart(LocalDateTime.now().minusHours(2));
        pastBookingEntity.setEnd(LocalDateTime.now().minusHours(1));
        bookingRepository.saveAndFlush(pastBookingEntity);

        List<BookingDto> result = bookingController.findAllBookingsByUser(booker.getId(), BookingState.PAST);

        assertEquals(1, result.size());
        assertEquals(pastBooking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByUserWithStateFuture() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto futureBooking = bookingController.add(booker.getId(), postDto);

        List<BookingDto> result = bookingController.findAllBookingsByUser(booker.getId(), BookingState.FUTURE);

        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByUserWithStateWaiting() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);

        List<BookingDto> result = bookingController.findAllBookingsByUser(booker.getId(), BookingState.WAITING);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByUserWithStateRejected() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);
        bookingController.approve(owner.getId(), booking.getId(), false);

        List<BookingDto> result = bookingController.findAllBookingsByUser(booker.getId(), BookingState.REJECTED);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByUserWithStateAll() {
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());

        UserDto owner1 = userController.create(createDefaultUser().build());
        ItemDto item1 = itemController.addItem(owner1.getId(), createDefaultItem().build());
        BookingPostDto postDto1 = createDefaultBooking(item1.getId()).build();
        BookingDto futureBooking = bookingController.add(booker.getId(), postDto1);
        bookingController.approve(owner1.getId(), futureBooking.getId(), false);

        UserDto owner2 = userController.create(createDefaultUser().email("owner2@yandex.ru").build());
        ItemDto item2 = itemController.addItem(owner2.getId(), createDefaultItem()
                .name("Молоток")
                .description("Хорошо разбивает кости, ой, то есть хорошо забивает гвозди")
                .build());
        BookingPostDto postDto2 = createDefaultBooking(item2.getId()).build();
        BookingDto pastBooking = bookingController.add(booker.getId(), postDto2);

        Booking pastBookingEntity = bookingRepository.findById(pastBooking.getId()).orElseThrow();
        pastBookingEntity.setStart(LocalDateTime.now().minusHours(2));
        pastBookingEntity.setEnd(LocalDateTime.now().minusHours(1));
        bookingRepository.saveAndFlush(pastBookingEntity);

        List<BookingDto> result = bookingController.findAllBookingsByUser(booker.getId(), BookingState.ALL);

        assertEquals(2, result.size());
        assertEquals(futureBooking.getId(), result.getFirst().getId());
        assertEquals(pastBooking.getId(), result.get(1).getId());
    }

    // ================================
    // ENDPOINT: findAllBookingsByOwner
    // ================================

    @Test
    void shouldFindAllBookingsByOwnerWithStateCurrent() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto currentBooking = bookingController.add(booker.getId(), postDto);

        Booking currentBookingEntity = bookingRepository.findById(currentBooking.getId()).orElseThrow();
        currentBookingEntity.setStart(LocalDateTime.now().minusHours(1));
        currentBookingEntity.setEnd(LocalDateTime.now().plusHours(1));
        bookingRepository.saveAndFlush(currentBookingEntity);

        List<BookingDto> result = bookingController.findAllBookingsByOwner(owner.getId(), BookingState.CURRENT);

        assertEquals(1, result.size());
        assertEquals(currentBooking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByOwnerWithStatePast() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto pastBooking = bookingController.add(booker.getId(), postDto);

        Booking pastBookingEntity = bookingRepository.findById(pastBooking.getId()).orElseThrow();
        pastBookingEntity.setStart(LocalDateTime.now().minusHours(2));
        pastBookingEntity.setEnd(LocalDateTime.now().minusHours(1));
        bookingRepository.saveAndFlush(pastBookingEntity);

        List<BookingDto> result = bookingController.findAllBookingsByOwner(owner.getId(), BookingState.PAST);

        assertEquals(1, result.size());
        assertEquals(pastBooking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByOwnerWithStateFuture() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto futureBooking = bookingController.add(booker.getId(), postDto);

        List<BookingDto> result = bookingController.findAllBookingsByOwner(owner.getId(), BookingState.FUTURE);

        assertEquals(1, result.size());
        assertEquals(futureBooking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByOwnerWithStateWaiting() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);

        List<BookingDto> result = bookingController.findAllBookingsByOwner(owner.getId(), BookingState.WAITING);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByOwnerWithStateRejected() {
        UserDto owner = userController.create(createDefaultUser().build());
        UserDto booker = userController.create(createDefaultUser().email("booker@yandex.ru").build());
        ItemDto item = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();

        BookingDto booking = bookingController.add(booker.getId(), postDto);
        bookingController.approve(owner.getId(), booking.getId(), false);

        List<BookingDto> result = bookingController.findAllBookingsByOwner(owner.getId(), BookingState.REJECTED);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.getFirst().getId());
    }

    @Test
    void shouldFindAllBookingsByOwnerWithStateAll() {
        UserDto owner = userController.create(createDefaultUser().build());

        UserDto booker1 = userController.create(createDefaultUser().email("booker1@yandex.ru").build());
        ItemDto item1 = itemController.addItem(owner.getId(), createDefaultItem().build());
        BookingPostDto postDto1 = createDefaultBooking(item1.getId()).build();
        BookingDto futureBooking = bookingController.add(booker1.getId(), postDto1);
        bookingController.approve(owner.getId(), futureBooking.getId(), false);

        UserDto booker2 = userController.create(createDefaultUser().email("booker2@yandex.ru").build());
        ItemDto item2 = itemController.addItem(owner.getId(), createDefaultItem()
                .name("Бензин из канистры")
                .description("Канистру не отдам, ищите тару!")
                .build());
        BookingPostDto postDto2 = createDefaultBooking(item2.getId()).build();
        BookingDto pastBooking = bookingController.add(booker2.getId(), postDto2);

        Booking pastBookingEntity = bookingRepository.findById(pastBooking.getId()).orElseThrow();
        pastBookingEntity.setStart(LocalDateTime.now().minusHours(2));
        pastBookingEntity.setEnd(LocalDateTime.now().minusHours(1));
        bookingRepository.saveAndFlush(pastBookingEntity);

        List<BookingDto> result = bookingController.findAllBookingsByOwner(owner.getId(), BookingState.ALL);

        assertEquals(2, result.size());
        assertEquals(futureBooking.getId(), result.getFirst().getId());
        assertEquals(pastBooking.getId(), result.get(1).getId());
    }

    @Test
    void shouldThrowExceptionWhenUserHasNotItem() {
        UserDto owner1 = userController.create(createDefaultUser().build());
        UserDto owner2 = userController.create(createDefaultUser().email("owner2@yandex.ru").build());
        UserDto booker = userController.create(createDefaultUser().email("booker1@yandex.ru").build());

        ItemDto item = itemController.addItem(owner1.getId(), createDefaultItem().build());
        BookingPostDto postDto = createDefaultBooking(item.getId()).build();
        bookingController.add(booker.getId(), postDto);

        assertThrows(ConditionsNotMetException.class, () -> bookingController.findAllBookingsByOwner(
                owner2.getId(), BookingState.ALL));
    }
}
