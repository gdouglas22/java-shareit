package ru.practicum.shareit.item.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCommentEligibilityValidatorTest {

    @Mock
    private BookingRepository bookingRepository;

    @Test
    void validateCanComment_throwsWhenNoApprovedPastBooking() {
        DefaultCommentEligibilityValidator validator = new DefaultCommentEligibilityValidator(bookingRepository);
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);
        when(bookingRepository.existsByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(1L, 2L, now, BookingStatus.APPROVED))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> validator.validateCanComment(1L, 2L, now));
    }

    @Test
    void validateCanComment_passesWhenApprovedPastBookingExists() {
        DefaultCommentEligibilityValidator validator = new DefaultCommentEligibilityValidator(bookingRepository);
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);
        when(bookingRepository.existsByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(1L, 2L, now, BookingStatus.APPROVED))
                .thenReturn(true);

        assertDoesNotThrow(() -> validator.validateCanComment(1L, 2L, now));
    }
}

