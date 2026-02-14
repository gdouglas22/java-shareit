package ru.practicum.shareit.booking.state;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultBookingStateResolverTest {

    private final DefaultBookingStateResolver resolver = new DefaultBookingStateResolver();

    @Test
    void resolve_returnsAllForNull() {
        assertEquals(BookingState.ALL, resolver.resolve(null));
    }

    @Test
    void resolve_handlesLowerCase() {
        assertEquals(BookingState.CURRENT, resolver.resolve("current"));
    }

    @Test
    void resolve_throwsForUnknownValue() {
        assertThrows(ValidationException.class, () -> resolver.resolve("nope"));
    }
}

