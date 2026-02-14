package ru.practicum.shareit.booking.policy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultBookingCreationPolicyTest {

    private final DefaultBookingCreationPolicy policy = new DefaultBookingCreationPolicy();

    @Test
    void checkCanBook_throwsWhenOwnerBooksOwnItem() {
        Item item = Item.builder()
                .id(1L)
                .owner(User.builder().id(10L).build())
                .available(true)
                .build();

        assertThrows(NotFoundException.class, () -> policy.checkCanBook(10L, item));
    }

    @Test
    void checkCanBook_throwsWhenItemUnavailable() {
        Item item = Item.builder()
                .id(1L)
                .owner(User.builder().id(20L).build())
                .available(false)
                .build();

        assertThrows(ValidationException.class, () -> policy.checkCanBook(10L, item));
    }

    @Test
    void checkCanBook_passesForAvailableForeignItem() {
        Item item = Item.builder()
                .id(1L)
                .owner(User.builder().id(20L).build())
                .available(true)
                .build();

        assertDoesNotThrow(() -> policy.checkCanBook(10L, item));
    }
}

