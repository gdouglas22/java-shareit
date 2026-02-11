package ru.practicum.shareit.booking.state;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FutureBookingStateStrategy implements BookingStateStrategy {

    private final BookingRepository bookingRepository;

    @Override
    public BookingState getState() {
        return BookingState.FUTURE;
    }

    @Override
    public List<Booking> findByBooker(Long bookerId, Sort sort, LocalDateTime now) {
        return bookingRepository.findByBooker_IdAndStartIsAfter(bookerId, now, sort);
    }

    @Override
    public List<Booking> findByOwner(Long ownerId, Sort sort, LocalDateTime now) {
        return bookingRepository.findByItem_Owner_IdAndStartIsAfter(ownerId, now, sort);
    }
}
