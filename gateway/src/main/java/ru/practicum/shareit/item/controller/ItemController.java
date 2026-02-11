package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.HeaderConstants;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HeaderConstants.USER_ID) Long ownerId,
                                         @RequestBody ItemDto itemDto) {
        validateCreate(itemDto);
        return itemClient.create(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(HeaderConstants.USER_ID) Long ownerId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        validateUpdate(itemDto);
        return itemClient.update(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable Long itemId,
                                          @RequestHeader(value = HeaderConstants.USER_ID, required = false)
                                          Long userId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader(HeaderConstants.USER_ID) Long ownerId) {
        return itemClient.getOwnerItems(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @RequestHeader(value = HeaderConstants.USER_ID, required = false)
                                         Long userId) {
        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(HeaderConstants.USER_ID) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentCreateDto commentCreateDto) {
        return itemClient.addComment(userId, itemId, commentCreateDto);
    }

    private void validateCreate(ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("Item body is required");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name is required");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description is required");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item availability is required");
        }
    }

    private void validateUpdate(ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("Item body is required");
        }
        if (itemDto.getName() != null && itemDto.getName().isBlank()) {
            throw new ValidationException("Item name must not be blank");
        }
        if (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description must not be blank");
        }
    }
}
