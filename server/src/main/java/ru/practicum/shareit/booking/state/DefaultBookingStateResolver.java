package ru.practicum.shareit.booking.state;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Locale;

@Component
public class DefaultBookingStateResolver implements BookingStateResolver {

    @Override
    public BookingState resolve(String state) {
        String value = state == null ? BookingState.ALL.name() : state.toUpperCase(Locale.ROOT);
        try {
            return BookingState.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
