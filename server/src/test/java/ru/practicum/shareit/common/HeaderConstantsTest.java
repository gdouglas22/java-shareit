package ru.practicum.shareit.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeaderConstantsTest {

    @Test
    void userIdHeader_hasExpectedName() {
        assertEquals("X-Sharer-User-Id", HeaderConstants.USER_ID);
    }
}

