package ru.practicum.shareit.booking.validation;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;

@Component
public class DefaultBookingValidator implements BookingValidator {

    @Override
    public void validateCreateRequest(BookingCreateRequestDto dto) {
        if (dto.getItemId() == null) {
            throw new ValidationException("нужно указать вещь для бронирования");
        }
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("нужно указать даты бронирования");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("дата окончания должна быть позже начала");
        }
    }

    @Override
    public void validateApproval(Booking booking) {
        if (booking.getStatus() == BookingStatus.APPROVED || booking.getStatus() == BookingStatus.REJECTED) {
            throw new ValidationException("статус уже изменён");
        }
    }
}
