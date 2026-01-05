package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookingModelTest {

    @Test
    void bookingBuilder_setsAllFields() {
        User booker = User.builder().id(1L).name("Booker").email("booker@mail.com").build();
        Item item = Item.builder().id(2L).name("Item").build();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        Booking booking = Booking.builder()
                .id(3L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        assertEquals(3L, booking.getId());
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(item, booking.getItem());
        assertEquals(booker, booking.getBooker());
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void bookingStatus_containsAllValues() {
        assertEquals(4, BookingStatus.values().length);
        assertNotNull(BookingStatus.valueOf("WAITING"));
    }
}
