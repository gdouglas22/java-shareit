package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMapperTest {

    @Test
    void toUserDto_whenUserNull_returnsNull() {
        assertNull(UserMapper.toUserDto(null));
    }

    @Test
    void toUserDto_mapsFields() {
        User user = User.builder().id(1L).name("Name").email("mail@mail.com").build();

        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
    }

    @Test
    void toUser_whenDtoNull_returnsNull() {
        assertNull(UserMapper.toUser(null));
    }

    @Test
    void toUser_mapsFields() {
        UserDto dto = UserDto.builder().id(2L).name("Name").email("mail@mail.com").build();

        User user = UserMapper.toUser(dto);

        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getName(), user.getName());
        assertEquals(dto.getEmail(), user.getEmail());
    }
}
