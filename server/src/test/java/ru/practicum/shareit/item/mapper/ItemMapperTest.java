package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
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
        Item item = Item.builder()
                .id(1L)
                .name("Hammer")
                .description("Heavy")
                .available(true)
                .owner(User.builder().id(2L).name("Owner").email("owner@mail.com").build())
                .requestId(5L)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
        assertEquals(item.getRequestId(), dto.getRequestId());
    }

    @Test
    void toItem_whenDtoNull_returnsNull() {
        assertNull(ItemMapper.toItem(null, null));
    }

    @Test
    void toItem_mapsFields() {
        User owner = User.builder().id(7L).name("Owner").email("owner@mail.com").build();
        ItemDto dto = ItemDto.builder()
                .id(9L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .requestId(3L)
                .build();

        Item item = ItemMapper.toItem(dto, owner);

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getDescription(), item.getDescription());
        assertEquals(dto.getAvailable(), item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertEquals(dto.getRequestId(), item.getRequestId());
    }
}
