package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto createItem(Long ownerId, ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("нужно передать данные вещи");
        }
        validateNewItem(itemDto);
        User owner = userService.getUserEntity(ownerId);

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = ItemRequest.builder().id(itemDto.getRequestId()).build();
        }

        Item item = ItemMapper.toItem(itemDto, owner, request);
        item.setId(null);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
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
    public ItemDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("вещь с id=" + itemId + " не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        userService.getUserEntity(ownerId);
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String query = text.toLowerCase(Locale.ROOT);
        return itemRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> containsIgnoreCase(item.getName(), query)
                        || containsIgnoreCase(item.getDescription(), query))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
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

    private boolean containsIgnoreCase(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }
}
