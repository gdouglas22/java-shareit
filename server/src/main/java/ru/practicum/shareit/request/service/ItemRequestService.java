package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto create(Long userId, ItemRequestCreateDto requestDto);

    List<ItemRequestResponseDto> getOwnRequests(Long userId);

    List<ItemRequestResponseDto> getOtherUsersRequests(Long userId, Integer from, Integer size);

    ItemRequestResponseDto getById(Long userId, Long requestId);
}
