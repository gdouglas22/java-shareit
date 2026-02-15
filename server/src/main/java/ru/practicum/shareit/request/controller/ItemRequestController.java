package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.common.HeaderConstants;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto create(@RequestHeader(HeaderConstants.USER_ID) Long userId,
                                         @RequestBody ItemRequestCreateDto requestDto) {
        return itemRequestService.create(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getOwnRequests(@RequestHeader(HeaderConstants.USER_ID) Long userId) {
        return itemRequestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getOtherUsersRequests(@RequestHeader(HeaderConstants.USER_ID) Long userId,
                                                              @RequestParam(required = false) Integer from,
                                                              @RequestParam(required = false) Integer size) {
        return itemRequestService.getOtherUsersRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(@RequestHeader(HeaderConstants.USER_ID) Long userId,
                                          @PathVariable Long requestId) {
        return itemRequestService.getById(userId, requestId);
    }
}
