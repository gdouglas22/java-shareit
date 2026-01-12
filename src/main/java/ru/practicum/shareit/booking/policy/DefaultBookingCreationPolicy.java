package ru.practicum.shareit.booking.policy;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

@Component
public class DefaultBookingCreationPolicy implements BookingCreationPolicy {

    @Override
    public void checkCanBook(Long bookerId, Item item) {
        if (item.getOwner() != null && item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("нельзя бронировать свою вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("вещь недоступна для бронирования");
        }
    }
}
