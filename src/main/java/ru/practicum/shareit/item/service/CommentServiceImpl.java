package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validation.CommentEligibilityValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserLookupService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;
    private final UserLookupService userService;
    private final CommentEligibilityValidator commentEligibilityValidator;
    private final Clock clock;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto) {
        if (commentCreateDto == null || commentCreateDto.getText() == null || commentCreateDto.getText().isBlank()) {
            throw new ValidationException("нельзя оставить пустой комментарий");
        }
        User author = userService.getUserEntity(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("вещь с id=" + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now(clock);
        commentEligibilityValidator.validateCanComment(itemId, userId, now);

        Comment comment = CommentMapper.toComment(commentCreateDto, item, author, now);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

    @Override
    public List<CommentDto> getItemComments(Long itemId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "created");
        return commentRepository.findByItem_Id(itemId, sort).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }
}
