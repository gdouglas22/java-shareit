package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

public interface ItemResponseAssembler {
    ItemResponseDto toItemResponse(Item item, Long requesterId);

    ItemOwnerListDto toOwnerList(Item item);
}
