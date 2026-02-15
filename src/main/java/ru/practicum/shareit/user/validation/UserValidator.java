package ru.practicum.shareit.user.validation;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserValidator {
    void validateForCreate(UserDto userDto);

    void validateForUpdate(UserDto userDto);

    void validateEmailUnique(String email, Long currentUserId);
}
