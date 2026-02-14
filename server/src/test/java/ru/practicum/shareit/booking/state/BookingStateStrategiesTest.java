package ru.practicum.shareit.booking.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingStateStrategiesTest {

    @Mock
    private BookingRepository bookingRepository;

    @Test
    void allStrategy_usesFindAllMethods() {
        AllBookingStateStrategy strategy = new AllBookingStateStrategy(bookingRepository);
        Sort sort = Sort.by("start");
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);
        List<Booking> expected = List.of(Booking.builder().id(1L).build());
        when(bookingRepository.findByBooker_Id(1L, sort)).thenReturn(expected);

        List<Booking> actual = strategy.findByBooker(1L, sort, now);

        assertEquals(BookingState.ALL, strategy.getState());
        assertSame(expected, actual);
        verify(bookingRepository).findByBooker_Id(1L, sort);
        strategy.findByOwner(2L, sort, now);
        verify(bookingRepository).findByItem_Owner_Id(2L, sort);
    }

    @Test
    void currentStrategy_usesCurrentMethods() {
        CurrentBookingStateStrategy strategy = new CurrentBookingStateStrategy(bookingRepository);
        Sort sort = Sort.by("start");
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);

        strategy.findByBooker(1L, sort, now);
        strategy.findByOwner(2L, sort, now);

        assertEquals(BookingState.CURRENT, strategy.getState());
        verify(bookingRepository).findByBooker_IdAndStartIsBeforeAndEndIsAfter(1L, now, now, sort);
        verify(bookingRepository).findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(2L, now, now, sort);
    }

    @Test
    void pastStrategy_usesPastMethods() {
        PastBookingStateStrategy strategy = new PastBookingStateStrategy(bookingRepository);
        Sort sort = Sort.by("start");
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);

        strategy.findByBooker(1L, sort, now);
        strategy.findByOwner(2L, sort, now);

        assertEquals(BookingState.PAST, strategy.getState());
        verify(bookingRepository).findByBooker_IdAndEndIsBefore(1L, now, sort);
        verify(bookingRepository).findByItem_Owner_IdAndEndIsBefore(2L, now, sort);
    }

    @Test
    void futureStrategy_usesFutureMethods() {
        FutureBookingStateStrategy strategy = new FutureBookingStateStrategy(bookingRepository);
        Sort sort = Sort.by("start");
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);

        strategy.findByBooker(1L, sort, now);
        strategy.findByOwner(2L, sort, now);

        assertEquals(BookingState.FUTURE, strategy.getState());
        verify(bookingRepository).findByBooker_IdAndStartIsAfter(1L, now, sort);
        verify(bookingRepository).findByItem_Owner_IdAndStartIsAfter(2L, now, sort);
    }

    @Test
    void waitingStrategy_usesStatusWaitingMethods() {
        WaitingBookingStateStrategy strategy = new WaitingBookingStateStrategy(bookingRepository);
        Sort sort = Sort.by("start");
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);

        strategy.findByBooker(1L, sort, now);
        strategy.findByOwner(2L, sort, now);

        assertEquals(BookingState.WAITING, strategy.getState());
        verify(bookingRepository).findByBooker_IdAndStatus(1L, BookingStatus.WAITING, sort);
        verify(bookingRepository).findByItem_Owner_IdAndStatus(2L, BookingStatus.WAITING, sort);
    }

    @Test
    void rejectedStrategy_usesStatusRejectedMethods() {
        RejectedBookingStateStrategy strategy = new RejectedBookingStateStrategy(bookingRepository);
        Sort sort = Sort.by("start");
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);

        strategy.findByBooker(1L, sort, now);
        strategy.findByOwner(2L, sort, now);

        assertEquals(BookingState.REJECTED, strategy.getState());
        verify(bookingRepository).findByBooker_IdAndStatus(1L, BookingStatus.REJECTED, sort);
        verify(bookingRepository).findByItem_Owner_IdAndStatus(2L, BookingStatus.REJECTED, sort);
    }
}

