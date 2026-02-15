package ru.practicum.shareit.booking.policy;

import ru.practicum.shareit.booking.model.Booking;

public interface BookingAccessPolicy {
    void checkCanApprove(Long ownerId, Booking booking);

    void checkCanView(Long userId, Booking booking);
}
