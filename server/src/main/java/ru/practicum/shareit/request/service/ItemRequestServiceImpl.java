package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserLookupService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 20;

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserLookupService userService;
    private final Clock clock;

    @Override
    @Transactional
    public ItemRequestResponseDto create(Long userId, ItemRequestCreateDto requestDto) {
        if (requestDto == null || requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ValidationException("description must not be blank");
        }

        User requestor = userService.getUserEntity(userId);
        ItemRequest itemRequest = ItemRequest.builder()
                .description(requestDto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now(clock))
                .build();
        ItemRequest saved = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toResponseDto(saved, List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        userService.getUserEntity(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        return toResponse(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getOtherUsersRequests(Long userId, Integer from, Integer size) {
        userService.getUserEntity(userId);

        int offset = from == null ? DEFAULT_FROM : from;
        int pageSize = size == null ? DEFAULT_SIZE : size;
        validatePagination(offset, pageSize);

        PageRequest pageRequest = PageRequest.of(offset / pageSize, pageSize, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdNot(userId, pageRequest);
        return toResponse(requests);
    }

    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        userService.getUserEntity(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("request with id=" + requestId + " not found"));
        List<ItemRequestItemDto> items = itemRepository
                .findByRequestId(requestId, Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(ItemRequestMapper::toRequestItemDto)
                .collect(Collectors.toList());
        return ItemRequestMapper.toResponseDto(request, items);
    }

    private void validatePagination(int from, int size) {
        if (from < 0) {
            throw new ValidationException("from must be >= 0");
        }
        if (size <= 0) {
            throw new ValidationException("size must be > 0");
        }
    }

    private List<ItemRequestResponseDto> toResponse(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        Set<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toSet());

        Map<Long, List<ItemRequestItemDto>> itemsByRequestId = itemRepository
                .findByRequestIdIn(requestIds, Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(ItemRequestMapper::toRequestItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> ItemRequestMapper.toResponseDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }
}
