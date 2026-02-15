package ru.practicum.shareit.item.validation;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

@Component
public class DefaultCommentEligibilityValidator implements CommentEligibilityValidator {

    private final BookingRepository bookingRepository;

    public DefaultCommentEligibilityValidator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validateCanComment(Long itemId, Long userId, LocalDateTime now) {
        boolean rented = bookingRepository.existsByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(
                itemId, userId, now, BookingStatus.APPROVED);
        if (!rented) {
            throw new ValidationException("комментарий может оставить только тот, кто арендовал вещь");
        }
    }
}
