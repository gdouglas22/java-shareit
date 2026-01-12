package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.validation.UserValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService, UserLookupService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("нужно передать данные пользователя");
        }
        userValidator.validateForCreate(userDto);
        userValidator.validateEmailUnique(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        user.setId(null);
        User saved = userRepository.save(user);
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("нужно передать данные пользователя");
        }
        User existing = getUserEntity(userId);
        userValidator.validateForUpdate(userDto);

        if (userDto.getEmail() != null) {
            userValidator.validateEmailUnique(userDto.getEmail(), userId);
            existing.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        User saved = userRepository.save(existing);
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        getUserEntity(userId);
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getUser(Long userId) {
        return UserMapper.toUserDto(getUserEntity(userId));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("пользователь с id=" + userId + " не найден"));
    }
}
