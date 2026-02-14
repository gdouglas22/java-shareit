package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemMapperResponseTest {

    @Test
    void toItemResponseDto_returnsNullForNullItem() {
        assertNull(ItemMapper.toItemResponseDto(null, null, null, List.of()));
    }

    @Test
    void toItemResponseDto_mapsFields() {
        Item item = Item.builder().id(1L).name("Item").description("Desc").available(true).requestId(2L).build();
        BookingShortDto last = BookingShortDto.builder().id(5L).build();
        BookingShortDto next = BookingShortDto.builder().id(6L).build();
        List<CommentDto> comments = List.of(CommentDto.builder()
                .id(1L)
                .text("Text")
                .created(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build());

        ItemResponseDto response = ItemMapper.toItemResponseDto(item, last, next, comments);

        assertEquals(1L, response.getId());
        assertEquals(5L, response.getLastBooking().getId());
        assertEquals(1, response.getComments().size());
    }

    @Test
    void toItemOwnerListDto_returnsNullForNullItem() {
        assertNull(ItemMapper.toItemOwnerListDto(null, null, null, List.of()));
    }

    @Test
    void toItemOwnerListDto_mapsFields() {
        Item item = Item.builder().id(2L).name("Owner item").description("Desc").available(true).requestId(3L).build();
        BookingShortDto last = BookingShortDto.builder().id(7L).build();
        BookingShortDto next = BookingShortDto.builder().id(8L).build();
        List<CommentDto> comments = List.of(CommentDto.builder().id(2L).text("Text").build());

        ItemOwnerListDto response = ItemMapper.toItemOwnerListDto(item, last, next, comments);

        assertEquals(2L, response.getId());
        assertEquals(8L, response.getNextBooking().getId());
        assertEquals(1, response.getComments().size());
    }
}

