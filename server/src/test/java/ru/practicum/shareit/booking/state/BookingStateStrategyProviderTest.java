package ru.practicum.shareit.booking.state;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingStateStrategyProviderTest {

    @Test
    void getStrategy_returnsExactStrategy() {
        BookingStateStrategy allStrategy = mock(BookingStateStrategy.class);
        BookingStateStrategy currentStrategy = mock(BookingStateStrategy.class);
        when(allStrategy.getState()).thenReturn(BookingState.ALL);
        when(currentStrategy.getState()).thenReturn(BookingState.CURRENT);

        BookingStateStrategyProvider provider = new BookingStateStrategyProvider(List.of(allStrategy, currentStrategy));

        assertSame(currentStrategy, provider.getStrategy(BookingState.CURRENT));
    }

    @Test
    void getStrategy_fallsBackToAllWhenStateMissing() {
        BookingStateStrategy allStrategy = mock(BookingStateStrategy.class);
        when(allStrategy.getState()).thenReturn(BookingState.ALL);

        BookingStateStrategyProvider provider = new BookingStateStrategyProvider(List.of(allStrategy));

        assertSame(allStrategy, provider.getStrategy(BookingState.FUTURE));
    }
}

