package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUser_whenDtoIsNull_throwsValidationException() {
        assertThrows(ValidationException.class, () -> userService.createUser(null));
    }

    @Test
    void createUser_whenNameIsBlank_throwsValidationException() {
        UserDto request = UserDto.builder()
                .email("blank-name@mail.com")
                .name(" ")
                .build();

        assertThrows(ValidationException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_whenEmailIsMissing_throwsValidationException() {
        UserDto request = UserDto.builder()
                .name("No Email")
                .build();

        assertThrows(ValidationException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_whenEmailHasNoAt_throwsValidationException() {
        UserDto request = UserDto.builder()
                .name("Bad Email")
                .email("invalid-email")
                .build();

        assertThrows(ValidationException.class, () -> userService.createUser(request));
    }

    @Test
    void createUser_whenEmailNotUnique_throwsConflictException() {
        UserDto existing = UserDto.builder()
                .name("Alex")
                .email("alex@mail.com")
                .build();
        userService.createUser(existing);

        UserDto duplicate = UserDto.builder()
                .name("Another Alex")
                .email("alex@mail.com")
                .build();

        assertThrows(ConflictException.class, () -> userService.createUser(duplicate));
    }

    @Test
    void createUser_whenDataValid_savesUser() {
        UserDto request = UserDto.builder()
                .name("Jane")
                .email("jane@mail.com")
                .build();

        UserDto created = userService.createUser(request);

        assertNotNull(created.getId());
        assertEquals("Jane", created.getName());
        assertEquals("jane@mail.com", created.getEmail());
        assertEquals(1, userRepository.findAll().size());
    }

    @Test
    void updateUser_whenDtoIsNull_throwsValidationException() {
        assertThrows(ValidationException.class, () -> userService.updateUser(1L, null));
    }

    @Test
    void updateUser_whenUserMissing_throwsNotFound() {
        UserDto changes = UserDto.builder().name("Ghost").build();

        assertThrows(NotFoundException.class, () -> userService.updateUser(999L, changes));
    }

    @Test
    void updateUser_whenEmailBlank_throwsValidationException() {
        UserDto created = userService.createUser(UserDto.builder()
                .name("Sam")
                .email("sam@mail.com")
                .build());

        UserDto changes = UserDto.builder().email(" ").build();

        assertThrows(ValidationException.class, () -> userService.updateUser(created.getId(), changes));
    }

    @Test
    void updateUser_whenEmailInvalid_throwsValidationException() {
        UserDto created = userService.createUser(UserDto.builder()
                .name("Sam")
                .email("sam@mail.com")
                .build());

        UserDto changes = UserDto.builder().email("invalid").build();

        assertThrows(ValidationException.class, () -> userService.updateUser(created.getId(), changes));
    }

    @Test
    void updateUser_whenEmailNotUnique_throwsConflict() {
        UserDto first = userService.createUser(UserDto.builder()
                .name("First")
                .email("first@mail.com")
                .build());
        UserDto second = userService.createUser(UserDto.builder()
                .name("Second")
                .email("second@mail.com")
                .build());

        UserDto duplicateEmail = UserDto.builder().email(first.getEmail()).build();

        assertThrows(ConflictException.class, () -> userService.updateUser(second.getId(), duplicateEmail));
    }

    @Test
    void updateUser_whenNameBlank_throwsValidationException() {
        UserDto created = userService.createUser(UserDto.builder()
                .name("Sam")
                .email("sam@mail.com")
                .build());

        UserDto changes = UserDto.builder().name(" ").build();

        assertThrows(ValidationException.class, () -> userService.updateUser(created.getId(), changes));
    }

    @Test
    void updateUser_whenEmailSameAsExisting_allowed() {
        UserDto created = userService.createUser(UserDto.builder()
                .name("Lena")
                .email("lena@mail.com")
                .build());

        UserDto changes = UserDto.builder()
                .email("lena@mail.com")
                .name("Lena Updated")
                .build();

        UserDto updated = userService.updateUser(created.getId(), changes);

        assertEquals("Lena Updated", updated.getName());
        assertEquals("lena@mail.com", updated.getEmail());
    }

    @Test
    void updateUser_whenDataValid_updatesFields() {
        UserDto created = userService.createUser(UserDto.builder()
                .name("Old Name")
                .email("old@mail.com")
                .build());

        UserDto changes = UserDto.builder()
                .name("New Name")
                .email("new@mail.com")
                .build();

        UserDto updated = userService.updateUser(created.getId(), changes);

        assertEquals(created.getId(), updated.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("new@mail.com", updated.getEmail());
    }

    @Test
    void deleteUser_whenUserMissing_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(123L));
    }

    @Test
    void deleteUser_whenUserExists_removedFromRepository() {
        UserDto created = userService.createUser(UserDto.builder()
                .name("To Delete")
                .email("delete@mail.com")
                .build());

        userService.deleteUser(created.getId());

        assertFalse(userRepository.findById(created.getId()).isPresent());
    }

    @Test
    void getUser_whenUserMissing_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getUser(404L));
    }

    @Test
    void getUser_whenUserExists_returnsDto() {
        UserDto created = userService.createUser(UserDto.builder()
                .name("Getter")
                .email("getter@mail.com")
                .build());

        UserDto found = userService.getUser(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
        assertEquals(created.getEmail(), found.getEmail());
    }

    @Test
    void getAllUsers_returnsAllSaved() {
        userService.createUser(UserDto.builder()
                .name("First")
                .email("first@mail.com")
                .build());
        userService.createUser(UserDto.builder()
                .name("Second")
                .email("second@mail.com")
                .build());

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }
}
