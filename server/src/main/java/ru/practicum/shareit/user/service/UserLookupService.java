package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

public interface UserLookupService {
    User getUserEntity(Long userId);
}
