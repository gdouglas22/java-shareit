package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

     void deleteUser(Long userId);

    UserDto getUser(Long userId);

    List<UserDto> getAllUsers();

    User getUserEntity(Long userId);
}
