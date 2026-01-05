package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void create_returnsCreatedUser() {
        UserDto request = UserDto.builder().name("User").email("user@mail.com").build();
        UserDto response = UserDto.builder().id(1L).name("User").email("user@mail.com").build();
        when(userService.createUser(request)).thenReturn(response);

        UserDto result = userController.create(request);

        assertEquals(response, result);
        verify(userService).createUser(request);
    }

    @Test
    void update_returnsUpdatedUser() {
        UserDto patch = UserDto.builder().name("Updated").build();
        UserDto response = UserDto.builder().id(2L).name("Updated").email("user@mail.com").build();
        when(userService.updateUser(2L, patch)).thenReturn(response);

        UserDto result = userController.update(2L, patch);

        assertEquals(response, result);
        verify(userService).updateUser(2L, patch);
    }

    @Test
    void getById_returnsUser() {
        UserDto response = UserDto.builder().id(3L).name("Name").email("name@mail.com").build();
        when(userService.getUser(3L)).thenReturn(response);

        UserDto result = userController.getById(3L);

        assertEquals(response, result);
        verify(userService).getUser(3L);
    }

    @Test
    void delete_invokesService() {
        userController.delete(4L);

        verify(userService).deleteUser(4L);
    }

    @Test
    void getAll_returnsUsers() {
        List<UserDto> users = List.of(
                UserDto.builder().id(1L).name("First").email("first@mail.com").build(),
                UserDto.builder().id(2L).name("Second").email("second@mail.com").build()
        );
        when(userService.getAllUsers()).thenReturn(users);

        List<UserDto> result = userController.getAll();

        assertEquals(users, result);
        verify(userService).getAllUsers();
    }
}
