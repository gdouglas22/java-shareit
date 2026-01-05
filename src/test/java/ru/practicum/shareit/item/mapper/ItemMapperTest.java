package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemMapperTest {

    @Test
    void toItemDto_whenItemNull_returnsNull() {
        assertNull(ItemMapper.toItemDto(null));
    }

    @Test
    void toItemDto_mapsFields() {
        ItemRequest request = ItemRequest.builder().id(5L).description("Need hammer").build();
        Item item = Item.builder()
                .id(1L)
                .name("Hammer")
                .description("Heavy")
                .available(true)
                .owner(User.builder().id(2L).name("Owner").email("owner@mail.com").build())
                .request(request)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
        assertEquals(request.getId(), dto.getRequestId());
    }

    @Test
    void toItem_whenDtoNull_returnsNull() {
        assertNull(ItemMapper.toItem(null, null, null));
    }

    @Test
    void toItem_mapsFields() {
        ItemRequest request = ItemRequest.builder().id(3L).description("Request").build();
        User owner = User.builder().id(7L).name("Owner").email("owner@mail.com").build();
        ItemDto dto = ItemDto.builder()
                .id(9L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .requestId(request.getId())
                .build();

        Item item = ItemMapper.toItem(dto, owner, request);

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(request, item.getRequest());
    }
}
