package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.policy.ItemAccessPolicy;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserLookupService;
import ru.practicum.shareit.item.validation.ItemValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserLookupService userService;
    private final ItemValidator itemValidator;
    private final ItemAccessPolicy itemAccessPolicy;
    private final CommentService commentService;
    private final ItemResponseAssembler itemResponseAssembler;

    @Override
    @Transactional
    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("нужно передать данные вещи");
        }
        itemValidator.validateCreate(itemDto);
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
        itemAccessPolicy.checkOwner(existing, ownerId);
        itemValidator.validateUpdate(itemDto);

        if (itemDto.getName() != null) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
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
        return itemResponseAssembler.toItemResponse(item, requesterId);
    }

    @Override
    public List<ItemOwnerListDto> getItemsByOwner(Long ownerId) {
        userService.getUserEntity(ownerId);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Item> items = itemRepository.findByOwner_Id(ownerId, sort);
        return items.stream()
                .map(itemResponseAssembler::toOwnerList)
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
        return commentService.addComment(userId, itemId, commentCreateDto);
    }

}
