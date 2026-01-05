package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("нужно передать данные пользователя");
        }
        validateRequiredFields(userDto);
        ensureEmailUnique(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        user.setId(null);
        User saved = userRepository.save(user);
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("нужно передать данные пользователя");
        }
        User existing = getUserEntity(userId);

        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new ValidationException("нужно указать email");
            }
            validateEmailFormat(userDto.getEmail());
            ensureEmailUnique(userDto.getEmail(), userId);
            existing.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("нужно указать имя");
            }
            existing.setName(userDto.getName());
        }

        User saved = userRepository.save(existing);
        return UserMapper.toUserDto(saved);
    }

    @Override
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

    private void validateRequiredFields(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("нужно указать имя");
        }
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("нужно указать email");
        }
        validateEmailFormat(userDto.getEmail());
    }

    private void ensureEmailUnique(String email, Long currentUserId) {
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
