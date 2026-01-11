package ru.practicum.shareit.item.validation;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

@Component
public class DefaultItemValidator implements ItemValidator {

    @Override
    public void validateCreate(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("нужно указать название вещи");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("нужно указать описание вещи");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("нужно указать доступность вещи");
        }
    }

    @Override
    public void validateUpdate(ItemDto itemDto) {
        if (itemDto.getName() != null && itemDto.getName().isBlank()) {
            throw new ValidationException("нужно указать название вещи");
        }
        if (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) {
            throw new ValidationException("нужно указать описание вещи");
        }
    }
}
