package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.validation.UserValidator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_throwsWhenInputNull() {
        assertThrows(ValidationException.class, () -> userService.createUser(null));
    }

    @Test
    void createUser_savesMappedUserWithNullId() {
        UserDto dto = UserDto.builder().id(100L).name("User").email("user@mail.com").build();
        final boolean[] idWasNullBeforeSave = {false};
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            idWasNullBeforeSave[0] = saved.getId() == null;
            saved.setId(1L);
            return saved;
        });

        UserDto response = userService.createUser(dto);

        assertEquals(1L, response.getId());
        assertEquals("User", response.getName());
        assertEquals("user@mail.com", response.getEmail());
        verify(userValidator).validateForCreate(dto);
        verify(userValidator).validateEmailUnique("user@mail.com", null);
        verify(userRepository).save(any(User.class));
        assertTrue(idWasNullBeforeSave[0]);
    }

    @Test
    void updateUser_throwsWhenInputNull() {
        assertThrows(ValidationException.class, () -> userService.updateUser(1L, null));
    }

    @Test
    void updateUser_updatesOnlyProvidedFields() {
        User existing = User.builder().id(1L).name("Old").email("old@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        UserDto patch = UserDto.builder().name("New Name").build();

        UserDto response = userService.updateUser(1L, patch);

        assertEquals("New Name", response.getName());
        assertEquals("old@mail.com", response.getEmail());
        verify(userValidator).validateForUpdate(patch);
        verify(userValidator, never()).validateEmailUnique(any(), any());
    }

    @Test
    void updateUser_validatesAndUpdatesEmail() {
        User existing = User.builder().id(1L).name("Name").email("old@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        UserDto patch = UserDto.builder().email("new@mail.com").build();

        UserDto response = userService.updateUser(1L, patch);

        assertEquals("new@mail.com", response.getEmail());
        verify(userValidator).validateEmailUnique("new@mail.com", 1L);
    }

    @Test
    void deleteUser_throwsWhenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void deleteUser_deletesByIdWhenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void getAllUsers_mapsRepositoryResult() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(1L).name("A").email("a@mail.com").build(),
                User.builder().id(2L).name("B").email("b@mail.com").build()
        ));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("A", users.get(0).getName());
        assertEquals("b@mail.com", users.get(1).getEmail());
    }
}
