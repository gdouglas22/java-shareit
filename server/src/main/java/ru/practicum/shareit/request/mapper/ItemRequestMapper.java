package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestMapper {

    public static ItemRequestResponseDto toResponseDto(ItemRequest request, List<ItemRequestItemDto> items) {
        if (request == null) {
            return null;
        }
        return ItemRequestResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }

    public static ItemRequestItemDto toRequestItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return ItemRequestItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner() == null ? null : item.getOwner().getId())
                .build();
    }
}
