package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemRequestMapperTest {

    @Test
    void toResponseDto_returnsNullForNullRequest() {
        assertNull(ItemRequestMapper.toResponseDto(null, List.of()));
    }

    @Test
    void toResponseDto_mapsRequestAndItems() {
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .description("Need item")
                .created(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build();
        List<ItemRequestItemDto> items = List.of(ItemRequestItemDto.builder().id(3L).name("Drill").build());

        ItemRequestResponseDto response = ItemRequestMapper.toResponseDto(request, items);

        assertEquals(10L, response.getId());
        assertEquals("Need item", response.getDescription());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void toRequestItemDto_returnsNullForNullItem() {
        assertNull(ItemRequestMapper.toRequestItemDto(null));
    }

    @Test
    void toRequestItemDto_mapsItemWithOwner() {
        Item item = Item.builder()
                .id(4L)
                .name("Drill")
                .owner(User.builder().id(8L).build())
                .build();

        ItemRequestItemDto dto = ItemRequestMapper.toRequestItemDto(item);

        assertEquals(4L, dto.getId());
        assertEquals(8L, dto.getOwnerId());
    }

    @Test
    void toRequestItemDto_handlesMissingOwner() {
        Item item = Item.builder().id(4L).name("Drill").owner(null).build();

        ItemRequestItemDto dto = ItemRequestMapper.toRequestItemDto(item);

        assertNull(dto.getOwnerId());
    }
}

