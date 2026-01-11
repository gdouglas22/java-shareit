package ru.practicum.shareit.booking.state;

import ru.practicum.shareit.booking.model.BookingState;

public interface BookingStateResolver {
    BookingState resolve(String state);
}
