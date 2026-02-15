package ru.practicum.shareit.item.policy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultItemAccessPolicyTest {

    private final DefaultItemAccessPolicy policy = new DefaultItemAccessPolicy();

    @Test
    void checkOwner_throwsWhenItemHasNoOwner() {
        Item item = Item.builder().id(1L).owner(null).build();

        assertThrows(ForbiddenException.class, () -> policy.checkOwner(item, 10L));
    }

    @Test
    void checkOwner_throwsWhenOwnerMismatch() {
        Item item = Item.builder().id(1L).owner(User.builder().id(11L).build()).build();

        assertThrows(ForbiddenException.class, () -> policy.checkOwner(item, 10L));
    }

    @Test
    void checkOwner_passesForOwner() {
        Item item = Item.builder().id(1L).owner(User.builder().id(10L).build()).build();

        assertDoesNotThrow(() -> policy.checkOwner(item, 10L));
    }
}

