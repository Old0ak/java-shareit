package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.storage.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingPostDto> json;

    @Autowired
    private JacksonTester<BookingDto> jsonResponse;

    @Test
    void testBookingPostDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 7, 21, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 21, 13, 0);

        BookingPostDto dto = BookingPostDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        JsonContent<BookingPostDto> result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.itemId");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
    }

    @Test
    void testBookingDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 7, 21, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 21, 13, 0);

        UserDto booker = UserDto.builder().id(2L).name("Booker").email("booker@yandex.ru").build();
        ItemDto item = ItemDto.builder().id(3L).name("Дрель").build();

        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();

        JsonContent<BookingDto> result = jsonResponse.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).hasJsonPathStringValue("$.status");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");

        assertThat(result).hasJsonPathNumberValue("$.booker.id");
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Booker");

        assertThat(result).hasJsonPathNumberValue("$.item.id");
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
    }
}
