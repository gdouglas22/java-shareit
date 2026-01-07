package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("нужно передать данные вещи");
        }
        validateNewItem(itemDto);
        User owner = userService.getUserEntity(ownerId);

        Item item = ItemMapper.toItem(itemDto, owner);
        item.setId(null);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("нужно передать данные вещи");
        }
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("вещь с id=" + itemId + " не найдена"));
        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("только владелец может менять вещь");
        }

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("нужно указать название вещи");
            }
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("нужно указать описание вещи");
            }
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        Item saved = itemRepository.save(existing);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemResponseDto getItem(Long itemId, Long requesterId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("вещь с id=" + itemId + " не найдена"));
        List<CommentDto> comments = loadComments(item.getId());

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        if (item.getOwner() != null && item.getOwner().getId().equals(requesterId)) {
            LocalDateTime now = LocalDateTime.now();
            lastBooking = BookingMapper.toBookingShortDto(
                    bookingRepository.findFirstByItem_IdAndEndIsBeforeAndStatusOrderByEndDesc(itemId, now, BookingStatus.APPROVED));
            nextBooking = BookingMapper.toBookingShortDto(
                    bookingRepository.findFirstByItem_IdAndStartIsAfterAndStatusOrderByStartAsc(itemId, now, BookingStatus.APPROVED));
        }
        return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemOwnerListDto> getItemsByOwner(Long ownerId) {
        userService.getUserEntity(ownerId);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Item> items = itemRepository.findByOwner_Id(ownerId, sort);
        LocalDateTime now = LocalDateTime.now();
        return items.stream()
                .map(item -> toOwnerListDto(item, now))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailable(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto) {
        if (commentCreateDto == null || commentCreateDto.getText() == null || commentCreateDto.getText().isBlank()) {
            throw new ValidationException("нельзя оставить пустой комментарий");
        }
        User author = userService.getUserEntity(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("вещь с id=" + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();
        boolean rented = bookingRepository.existsByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(
                itemId, userId, now, BookingStatus.APPROVED);
        if (!rented) {
            throw new ValidationException("комментарий может оставить только тот, кто арендовал вещь");
        }

        Comment comment = CommentMapper.toComment(commentCreateDto, item, author, now);
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

    private void validateNewItem(ItemDto itemDto) {
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

    private List<CommentDto> loadComments(Long itemId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "created");
        return commentRepository.findByItem_Id(itemId, sort).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private ItemOwnerListDto toOwnerListDto(Item item, LocalDateTime now) {
        BookingShortDto lastBooking = BookingMapper.toBookingShortDto(
                bookingRepository.findFirstByItem_IdAndEndIsBeforeAndStatusOrderByEndDesc(item.getId(), now, BookingStatus.APPROVED));
        BookingShortDto nextBooking = BookingMapper.toBookingShortDto(
                bookingRepository.findFirstByItem_IdAndStartIsAfterAndStatusOrderByStartAsc(item.getId(), now, BookingStatus.APPROVED));
        List<CommentDto> comments = loadComments(item.getId());
        return ItemMapper.toItemOwnerListDto(item, lastBooking, nextBooking, comments);
    }
}
