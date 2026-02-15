package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CommentMapperTest {

    @Test
    void toComment_returnsNullForNullInput() {
        assertNull(CommentMapper.toComment(null, null, null, null));
    }

    @Test
    void toComment_mapsFields() {
        CommentCreateDto createDto = CommentCreateDto.builder().text("Nice item").build();
        Item item = Item.builder().id(1L).build();
        User author = User.builder().id(2L).name("Author").build();
        LocalDateTime created = LocalDateTime.of(2026, 2, 14, 12, 0);

        Comment comment = CommentMapper.toComment(createDto, item, author, created);

        assertEquals("Nice item", comment.getText());
        assertEquals(item, comment.getItem());
        assertEquals(author, comment.getAuthor());
        assertEquals(created, comment.getCreated());
    }

    @Test
    void toCommentDto_returnsNullForNullInput() {
        assertNull(CommentMapper.toCommentDto(null));
    }

    @Test
    void toCommentDto_mapsAuthorName() {
        Comment comment = Comment.builder()
                .id(5L)
                .text("Text")
                .author(User.builder().id(1L).name("Author").build())
                .created(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build();

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertEquals(5L, dto.getId());
        assertEquals("Author", dto.getAuthorName());
    }

    @Test
    void toCommentDto_handlesNullAuthor() {
        Comment comment = Comment.builder().id(5L).text("Text").author(null).build();

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertNull(dto.getAuthorName());
    }
}

