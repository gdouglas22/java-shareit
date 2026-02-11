package ru.practicum.shareit.booking.state;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BookingStateStrategyProvider {

    private final Map<BookingState, BookingStateStrategy> strategies;

    public BookingStateStrategyProvider(List<BookingStateStrategy> strategies) {
        Map<BookingState, BookingStateStrategy> map = new EnumMap<>(BookingState.class);
        for (BookingStateStrategy strategy : strategies) {
            map.put(strategy.getState(), strategy);
        }
        this.strategies = map;
    }

    public BookingStateStrategy getStrategy(BookingState state) {
        BookingStateStrategy strategy = strategies.get(state);
        if (strategy == null) {
            return strategies.get(BookingState.ALL);
        }
        return strategy;
    }
}
