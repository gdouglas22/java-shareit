package ru.practicum.shareit.item.validation;

import java.time.LocalDateTime;

public interface CommentEligibilityValidator {
    void validateCanComment(Long itemId, Long userId, LocalDateTime now);
}
