package ru.practicum.shareit.booking.policy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultBookingAccessPolicyTest {

    private final DefaultBookingAccessPolicy policy = new DefaultBookingAccessPolicy();

    @Test
    void checkCanApprove_throwsWhenOwnerMismatch() {
        Booking booking = Booking.builder()
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();

        assertThrows(ForbiddenException.class, () -> policy.checkCanApprove(1L, booking));
    }

    @Test
    void checkCanApprove_passesForItemOwner() {
        Booking booking = Booking.builder()
                .item(Item.builder().owner(User.builder().id(1L).build()).build())
                .build();

        assertDoesNotThrow(() -> policy.checkCanApprove(1L, booking));
    }

    @Test
    void checkCanView_passesForBooker() {
        Booking booking = Booking.builder()
                .booker(User.builder().id(1L).build())
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();

        assertDoesNotThrow(() -> policy.checkCanView(1L, booking));
    }

    @Test
    void checkCanView_passesForOwner() {
        Booking booking = Booking.builder()
                .booker(User.builder().id(1L).build())
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();

        assertDoesNotThrow(() -> policy.checkCanView(2L, booking));
    }

    @Test
    void checkCanView_throwsForAnotherUser() {
        Booking booking = Booking.builder()
                .booker(User.builder().id(1L).build())
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();

        assertThrows(NotFoundException.class, () -> policy.checkCanView(3L, booking));
    }
}

