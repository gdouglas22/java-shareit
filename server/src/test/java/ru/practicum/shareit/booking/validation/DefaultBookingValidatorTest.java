package ru.practicum.shareit.booking.validation;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultBookingValidatorTest {

    private final DefaultBookingValidator validator = new DefaultBookingValidator();

    @Test
    void validateCreateRequest_throwsWhenItemIsMissing() {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .build();

        assertThrows(ValidationException.class, () -> validator.validateCreateRequest(dto));
    }

    @Test
    void validateCreateRequest_throwsWhenDatesMissing() {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .build();

        assertThrows(ValidationException.class, () -> validator.validateCreateRequest(dto));
    }

    @Test
    void validateCreateRequest_throwsWhenEndNotAfterStart() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 14, 10, 0);
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(start)
                .build();

        assertThrows(ValidationException.class, () -> validator.validateCreateRequest(dto));
    }

    @Test
    void validateCreateRequest_passesForValidRequest() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 14, 10, 0);
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(start.plusHours(2))
                .build();

        assertDoesNotThrow(() -> validator.validateCreateRequest(dto));
    }

    @Test
    void validateApproval_throwsForFinalStatuses() {
        Booking approved = Booking.builder().status(BookingStatus.APPROVED).build();
        Booking rejected = Booking.builder().status(BookingStatus.REJECTED).build();

        assertThrows(ValidationException.class, () -> validator.validateApproval(approved));
        assertThrows(ValidationException.class, () -> validator.validateApproval(rejected));
    }

    @Test
    void validateApproval_passesForWaitingStatus() {
        Booking waiting = Booking.builder().status(BookingStatus.WAITING).build();

        assertDoesNotThrow(() -> validator.validateApproval(waiting));
    }
}
