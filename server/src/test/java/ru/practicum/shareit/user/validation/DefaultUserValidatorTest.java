package ru.practicum.shareit.user.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void validateForCreate_throwsWhenNameIsBlank() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);
        UserDto dto = UserDto.builder().name(" ").email("mail@mail.com").build();

        assertThrows(ValidationException.class, () -> validator.validateForCreate(dto));
    }

    @Test
    void validateForCreate_throwsWhenEmailInvalid() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);
        UserDto dto = UserDto.builder().name("Name").email("mail.mail.com").build();

        assertThrows(ValidationException.class, () -> validator.validateForCreate(dto));
    }

    @Test
    void validateForCreate_passesForValidData() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);
        UserDto dto = UserDto.builder().name("Name").email("mail@mail.com").build();

        assertDoesNotThrow(() -> validator.validateForCreate(dto));
    }

    @Test
    void validateForUpdate_throwsWhenNameBlank() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);
        UserDto dto = UserDto.builder().name(" ").build();

        assertThrows(ValidationException.class, () -> validator.validateForUpdate(dto));
    }

    @Test
    void validateForUpdate_throwsWhenEmailInvalid() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);
        UserDto dto = UserDto.builder().email("mail.mail.com").build();

        assertThrows(ValidationException.class, () -> validator.validateForUpdate(dto));
    }

    @Test
    void validateEmailUnique_returnsWhenEmailNull() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);

        validator.validateEmailUnique(null, 1L);

        verifyNoInteractions(userRepository);
    }

    @Test
    void validateEmailUnique_throwsWhenEmailBelongsToAnotherUser() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);
        when(userRepository.findByEmail("mail@mail.com"))
                .thenReturn(Optional.of(User.builder().id(2L).build()));

        assertThrows(ConflictException.class, () -> validator.validateEmailUnique("mail@mail.com", 1L));
    }

    @Test
    void validateEmailUnique_passesForCurrentUser() {
        DefaultUserValidator validator = new DefaultUserValidator(userRepository);
        when(userRepository.findByEmail("mail@mail.com"))
                .thenReturn(Optional.of(User.builder().id(1L).build()));

        assertDoesNotThrow(() -> validator.validateEmailUnique("mail@mail.com", 1L));
    }
}

