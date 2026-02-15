package ru.practicum.shareit.booking.policy;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;

@Component
public class DefaultBookingAccessPolicy implements BookingAccessPolicy {

    @Override
    public void checkCanApprove(Long ownerId, Booking booking) {
        if (booking.getItem() == null || booking.getItem().getOwner() == null
                || !booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("подтвердить бронирование может только владелец вещи");
        }
    }

    @Override
    public void checkCanView(Long userId, Booking booking) {
        Long ownerId = booking.getItem() != null && booking.getItem().getOwner() != null
                ? booking.getItem().getOwner().getId()
                : null;
        if (!booking.getBooker().getId().equals(userId) && !userId.equals(ownerId)) {
            throw new NotFoundException("бронирование не найдено для пользователя");
        }
    }
}
