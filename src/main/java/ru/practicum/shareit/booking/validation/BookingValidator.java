package ru.practicum.shareit.booking.validation;

import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.model.Booking;

public interface BookingValidator {
    void validateCreateRequest(BookingCreateRequestDto dto);

    void validateApproval(Booking booking);
}
