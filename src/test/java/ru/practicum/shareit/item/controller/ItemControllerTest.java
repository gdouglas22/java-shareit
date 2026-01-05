package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @Test
    void create_returnsCreatedItem() {
        ItemDto request = ItemDto.builder()
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build();
        ItemDto response = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build();
        when(itemService.createItem(10L, request)).thenReturn(response);

        ItemDto result = itemController.create(10L, request);

        assertEquals(response, result);
        verify(itemService).createItem(10L, request);
    }

    @Test
    void update_returnsUpdatedItem() {
        ItemDto patch = ItemDto.builder().name("Updated").build();
        ItemDto response = ItemDto.builder().id(2L).name("Updated").available(true).build();
        when(itemService.updateItem(5L, 2L, patch)).thenReturn(response);

        ItemDto result = itemController.update(5L, 2L, patch);

        assertEquals(response, result);
        verify(itemService).updateItem(5L, 2L, patch);
    }

    @Test
    void getById_returnsItem() {
        ItemDto response = ItemDto.builder().id(3L).name("Item").build();
        when(itemService.getItem(3L)).thenReturn(response);

        ItemDto result = itemController.getById(3L, null);

        assertEquals(response, result);
        verify(itemService).getItem(3L);
    }

    @Test
    void getOwnerItems_returnsItems() {
        List<ItemDto> items = List.of(
                ItemDto.builder().id(1L).name("One").build(),
                ItemDto.builder().id(2L).name("Two").build()
        );
        when(itemService.getItemsByOwner(7L)).thenReturn(items);

        List<ItemDto> result = itemController.getOwnerItems(7L);

        assertEquals(items, result);
        verify(itemService).getItemsByOwner(7L);
    }

    @Test
    void search_returnsResults() {
        List<ItemDto> items = List.of(ItemDto.builder().id(5L).name("Match").build());
        when(itemService.searchItems("text")).thenReturn(items);

        List<ItemDto> result = itemController.search("text", null);

        assertEquals(items, result);
        verify(itemService).searchItems("text");
    }
}
