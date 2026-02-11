package ru.practicum.shareit.booking.state;

import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStateStrategy {
    BookingState getState();

    List<Booking> findByBooker(Long bookerId, Sort sort, LocalDateTime now);

    List<Booking> findByOwner(Long ownerId, Sort sort, LocalDateTime now);
}
