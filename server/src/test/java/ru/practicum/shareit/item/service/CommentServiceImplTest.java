package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validation.CommentEligibilityValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserLookupService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserLookupService userService;

    @Mock
    private CommentEligibilityValidator commentEligibilityValidator;

    @Mock
    private Clock clock;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void addComment_throwsWhenTextBlank() {
        CommentCreateDto dto = CommentCreateDto.builder().text(" ").build();

        assertThrows(ValidationException.class, () -> commentService.addComment(1L, 2L, dto));
    }

    @Test
    void addComment_throwsWhenItemNotFound() {
        when(userService.getUserEntity(1L)).thenReturn(User.builder().id(1L).build());
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        CommentCreateDto dto = CommentCreateDto.builder().text("Nice").build();

        assertThrows(NotFoundException.class, () -> commentService.addComment(1L, 2L, dto));
    }

    @Test
    void addComment_savesAndReturnsDto() {
        User author = User.builder().id(1L).name("Author").email("a@mail.com").build();
        Item item = Item.builder().id(2L).name("Item").build();
        Instant now = Instant.parse("2026-02-14T12:00:00Z");
        LocalDateTime created = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        CommentCreateDto dto = CommentCreateDto.builder().text("Great item").build();

        when(userService.getUserEntity(1L)).thenReturn(author);
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(clock.instant()).thenReturn(now);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(99L);
            return comment;
        });

        CommentDto result = commentService.addComment(1L, 2L, dto);

        assertEquals(99L, result.getId());
        assertEquals("Great item", result.getText());
        assertEquals("Author", result.getAuthorName());
        assertEquals(created, result.getCreated());
        verify(commentEligibilityValidator).validateCanComment(2L, 1L, created);
    }

    @Test
    void getItemComments_mapsRepositoryResult() {
        User author = User.builder().id(1L).name("Author").build();
        Comment comment = Comment.builder()
                .id(5L)
                .text("Text")
                .author(author)
                .created(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build();
        when(commentRepository.findByItem_Id(eq(10L), any(Sort.class))).thenReturn(List.of(comment));

        List<CommentDto> result = commentService.getItemComments(10L);

        assertEquals(1, result.size());
        assertEquals("Text", result.get(0).getText());
        assertEquals("Author", result.get(0).getAuthorName());
    }
}

