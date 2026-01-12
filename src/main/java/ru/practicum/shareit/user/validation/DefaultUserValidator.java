package ru.practicum.shareit.user.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultUserValidator implements UserValidator {

    private final UserRepository userRepository;

    @Override
    public void validateForCreate(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("нужно указать имя");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("нужно указать email");
        }
        validateEmailFormat(userDto.getEmail());
    }

    @Override
    public void validateForUpdate(UserDto userDto) {
        if (userDto.getName() != null && userDto.getName().isBlank()) {
            throw new ValidationException("нужно указать имя");
        }
        if (userDto.getEmail() != null && userDto.getEmail().isBlank()) {
            throw new ValidationException("нужно указать email");
        }
        if (userDto.getEmail() != null) {
            validateEmailFormat(userDto.getEmail());
        }
    }

    @Override
    public void validateEmailUnique(String email, Long currentUserId) {
        if (email == null) {
            return;
        }
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent() && !byEmail.get().getId().equals(currentUserId)) {
            throw new ConflictException("такой email уже занят");
        }
    }

    private void validateEmailFormat(String email) {
        if (!email.contains("@")) {
            throw new ValidationException("email должен содержать '@'");
        }
    }
}
