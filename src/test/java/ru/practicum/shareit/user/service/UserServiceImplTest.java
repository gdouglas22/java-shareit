package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_whenDtoNull_throwsValidationException() {
        assertThrows(ValidationException.class, () -> userService.createUser(null));
    }

    @Test
    void createUser_whenEmailExists_throwsConflict() {
        UserDto dto = UserDto.builder().name("John").email("mail@mail.com").build();
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(User.builder().id(1L).build()));

        assertThrows(ConflictException.class, () -> userService.createUser(dto));
    }

    @Test
    void createUser_whenValid_returnsSaved() {
        UserDto dto = UserDto.builder().name("John").email("mail@mail.com").build();
        User saved = User.builder().id(5L).name(dto.getName()).email(dto.getEmail()).build();
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.createUser(dto);

        assertEquals(saved.getId(), result.getId());
        assertEquals(saved.getEmail(), result.getEmail());
    }

    @Test
    void updateUser_whenNotFound_throws() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(9L, UserDto.builder().name("n").build()));
    }

    @Test
    void updateUser_whenEmailConflict_throws() {
        User existing = User.builder().id(2L).name("Old").email("old@mail.com").build();
        when(userRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@mail.com")).thenReturn(Optional.of(User.builder().id(3L).build()));

        assertThrows(ConflictException.class,
                () -> userService.updateUser(existing.getId(), UserDto.builder().email("new@mail.com").build()));
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(1L).name("A").email("a@mail.com").build(),
                User.builder().id(2L).name("B").email("b@mail.com").build()
        ));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }
}
