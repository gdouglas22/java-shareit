package ru.practicum.shareit.booking.policy;

import ru.practicum.shareit.item.model.Item;

public interface BookingCreationPolicy {
    void checkCanBook(Long bookerId, Item item);
}
