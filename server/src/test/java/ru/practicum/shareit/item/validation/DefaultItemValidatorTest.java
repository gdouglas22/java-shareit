package ru.practicum.shareit.item.validation;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultItemValidatorTest {

    private final DefaultItemValidator validator = new DefaultItemValidator();

    @Test
    void validateCreate_throwsWhenNameBlank() {
        ItemDto dto = ItemDto.builder()
                .name(" ")
                .description("desc")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> validator.validateCreate(dto));
    }

    @Test
    void validateCreate_throwsWhenDescriptionBlank() {
        ItemDto dto = ItemDto.builder()
                .name("name")
                .description("")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> validator.validateCreate(dto));
    }

    @Test
    void validateCreate_throwsWhenAvailabilityMissing() {
        ItemDto dto = ItemDto.builder()
                .name("name")
                .description("desc")
                .available(null)
                .build();

        assertThrows(ValidationException.class, () -> validator.validateCreate(dto));
    }

    @Test
    void validateCreate_passesForValidData() {
        ItemDto dto = ItemDto.builder()
                .name("name")
                .description("desc")
                .available(true)
                .build();

        assertDoesNotThrow(() -> validator.validateCreate(dto));
    }

    @Test
    void validateUpdate_throwsWhenNameBlank() {
        ItemDto dto = ItemDto.builder().name(" ").build();

        assertThrows(ValidationException.class, () -> validator.validateUpdate(dto));
    }

    @Test
    void validateUpdate_throwsWhenDescriptionBlank() {
        ItemDto dto = ItemDto.builder().description(" ").build();

        assertThrows(ValidationException.class, () -> validator.validateUpdate(dto));
    }

    @Test
    void validateUpdate_passesForEmptyPatch() {
        ItemDto dto = ItemDto.builder().build();

        assertDoesNotThrow(() -> validator.validateUpdate(dto));
    }
}
