package ru.practicum.shareit.item.validation;

import ru.practicum.shareit.item.dto.ItemDto;

public interface ItemValidator {
    void validateCreate(ItemDto itemDto);

    void validateUpdate(ItemDto itemDto);
}
