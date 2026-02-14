package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BookingMapperTest {

    @Test
    void toBooking_returnsNullForNullInput() {
        assertNull(BookingMapper.toBooking(null, null, null, BookingStatus.WAITING));
    }

    @Test
    void toBooking_mapsFields() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 14, 10, 0);
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(10L)
                .start(start)
                .end(start.plusHours(2))
                .build();
        Item item = Item.builder().id(10L).name("Item").build();
        User booker = User.builder().id(1L).name("Booker").build();

        Booking booking = BookingMapper.toBooking(dto, item, booker, BookingStatus.WAITING);

        assertEquals(start, booking.getStart());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void toBookingResponseDto_returnsNullForNullInput() {
        assertNull(BookingMapper.toBookingResponseDto(null));
    }

    @Test
    void toBookingResponseDto_mapsNestedFields() {
        Booking booking = Booking.builder()
                .id(20L)
                .start(LocalDateTime.of(2026, 2, 14, 10, 0))
                .end(LocalDateTime.of(2026, 2, 14, 12, 0))
                .status(BookingStatus.APPROVED)
                .item(Item.builder().id(10L).name("Item").build())
                .booker(User.builder().id(1L).name("Booker").build())
                .build();

        BookingResponseDto dto = BookingMapper.toBookingResponseDto(booking);

        assertEquals(20L, dto.getId());
        assertEquals("Item", dto.getItem().getName());
        assertEquals("Booker", dto.getBooker().getName());
    }

    @Test
    void toBookingResponseDto_handlesNullNestedEntities() {
        Booking booking = Booking.builder()
                .id(20L)
                .status(BookingStatus.WAITING)
                .item(null)
                .booker(null)
                .build();

        BookingResponseDto dto = BookingMapper.toBookingResponseDto(booking);

        assertNull(dto.getItem());
        assertNull(dto.getBooker());
    }

    @Test
    void toBookingShortDto_returnsNullForNullInput() {
        assertNull(BookingMapper.toBookingShortDto(null));
    }

    @Test
    void toBookingShortDto_mapsBookerIdAndDates() {
        Booking booking = Booking.builder()
                .id(20L)
                .booker(User.builder().id(1L).build())
                .start(LocalDateTime.of(2026, 2, 14, 10, 0))
                .end(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build();

        BookingShortDto dto = BookingMapper.toBookingShortDto(booking);

        assertEquals(20L, dto.getId());
        assertEquals(1L, dto.getBookerId());
    }

    @Test
    void toBookingShortDto_handlesNullBooker() {
        Booking booking = Booking.builder().id(20L).booker(null).build();

        BookingShortDto dto = BookingMapper.toBookingShortDto(booking);

        assertNull(dto.getBookerId());
    }
}

