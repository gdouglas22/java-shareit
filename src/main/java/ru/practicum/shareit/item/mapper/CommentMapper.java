package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    public static Comment toComment(CommentCreateDto dto, Item item, User author, LocalDateTime created) {
        if (dto == null) {
            return null;
        }
        return Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(created)
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .created(comment.getCreated())
                .build();
    }
}
