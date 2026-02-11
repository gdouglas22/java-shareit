package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto);

    List<CommentDto> getItemComments(Long itemId);
}
