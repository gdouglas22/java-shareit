package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.InMemoryItemRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    private ItemService itemService;
    private User owner;

    @BeforeEach
    void setUp() {
        itemRepository = new InMemoryItemRepository();
        itemService = new ItemServiceImpl(itemRepository, userService);
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();
    }

    @Test
    void createItem_whenDtoIsNull_throwsValidationException() {
        assertThrows(ValidationException.class, () -> itemService.createItem(owner.getId(), null));
    }

    @Test
    void createItem_whenNameBlank_throwsValidationException() {
        ItemDto request = ItemDto.builder()
                .name(" ")
                .description("desc")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> itemService.createItem(owner.getId(), request));
    }

    @Test
    void createItem_whenDescriptionBlank_throwsValidationException() {
        ItemDto request = ItemDto.builder()
                .name("Item")
                .description(" ")
                .available(true)
                .build();

        assertThrows(ValidationException.class, () -> itemService.createItem(owner.getId(), request));
    }

    @Test
    void createItem_whenAvailabilityMissing_throwsValidationException() {
        ItemDto request = ItemDto.builder()
                .name("Item")
                .description("desc")
                .build();

        assertThrows(ValidationException.class, () -> itemService.createItem(owner.getId(), request));
    }

    @Test
    void createItem_whenDataValid_savesItemWithOwnerAndRequest() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        ItemDto request = ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .requestId(5L)
                .build();

        ItemDto created = itemService.createItem(owner.getId(), request);

        verify(userService).getUserEntity(owner.getId());
        assertNotNull(created.getId());
        assertEquals("Drill", created.getName());
        assertEquals(5L, created.getRequestId());
        assertEquals(1, itemRepository.findAllByOwnerId(owner.getId()).size());
    }

    @Test
    void updateItem_whenDtoIsNull_throwsValidationException() {
        assertThrows(ValidationException.class, () -> itemService.updateItem(owner.getId(), 1L, null));
    }

    @Test
    void updateItem_whenItemMissing_throwsNotFound() {
        ItemDto changes = ItemDto.builder().name("Updated").build();

        assertThrows(NotFoundException.class, () -> itemService.updateItem(owner.getId(), 42L, changes));
    }

    @Test
    void updateItem_whenNotOwner_throwsForbidden() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        ItemDto request = ItemDto.builder()
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(owner.getId(), request);

        ItemDto changes = ItemDto.builder().name("Changed").build();

        assertThrows(ForbiddenException.class, () -> itemService.updateItem(999L, created.getId(), changes));
    }

    @Test
    void updateItem_whenNameBlank_throwsValidationException() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        ItemDto created = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .build());

        ItemDto changes = ItemDto.builder().name(" ").build();

        assertThrows(ValidationException.class, () -> itemService.updateItem(owner.getId(), created.getId(), changes));
    }

    @Test
    void updateItem_whenDescriptionBlank_throwsValidationException() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        ItemDto created = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .build());

        ItemDto changes = ItemDto.builder().description("").build();

        assertThrows(ValidationException.class, () -> itemService.updateItem(owner.getId(), created.getId(), changes));
    }

    @Test
    void updateItem_whenDataValid_updatesFields() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        ItemDto created = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(false)
                .build());

        ItemDto changes = ItemDto.builder()
                .name("Light Hammer")
                .description("Lighter hammer")
                .available(true)
                .build();

        ItemDto updated = itemService.updateItem(owner.getId(), created.getId(), changes);

        assertEquals("Light Hammer", updated.getName());
        assertEquals("Lighter hammer", updated.getDescription());
        assertEquals(Boolean.TRUE, updated.getAvailable());
    }

    @Test
    void getItem_whenMissing_throwsNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.getItem(100L));
    }

    @Test
    void getItem_whenExists_returnsDto() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        ItemDto created = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build());

        ItemDto found = itemService.getItem(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Hammer", found.getName());
        assertEquals("Heavy hammer", found.getDescription());
    }

    @Test
    void getItemsByOwner_returnsOnlyOwnerItems() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build());
        itemRepository.save(Item.builder()
                .name("Foreign")
                .description("Other owner")
                .available(true)
                .owner(User.builder().id(99L).name("Other").email("other@mail.com").build())
                .build());

        List<ItemDto> items = itemService.getItemsByOwner(owner.getId());
        Set<String> names = items.stream().map(ItemDto::getName).collect(Collectors.toSet());

        assertEquals(1, items.size());
        assertTrue(names.contains("Hammer"));
    }

    @Test
    void getItemsByOwner_whenUserMissing_propagatesException() {
        when(userService.getUserEntity(owner.getId())).thenThrow(new NotFoundException("not found"));

        assertThrows(NotFoundException.class, () -> itemService.getItemsByOwner(owner.getId()));
    }

    @Test
    void searchItems_whenTextBlank_returnsEmptyList() {
        assertTrue(itemService.searchItems(" ").isEmpty());
        assertTrue(itemService.searchItems(null).isEmpty());
    }

    @Test
    void searchItems_returnsOnlyAvailableMatchesIgnoringCase() {
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Cordless Drill")
                .description("Powerful tool")
                .available(true)
                .build());
        itemRepository.save(Item.builder()
                .name(null)
                .description("DRILL bit set")
                .available(true)
                .owner(owner)
                .build());
        itemRepository.save(Item.builder()
                .name("Old drill")
                .description("Unavailable item")
                .available(false)
                .owner(owner)
                .build());

        List<ItemDto> results = itemService.searchItems("drill");
        Set<String> names = results.stream()
                .map(ItemDto::getName)
                .collect(Collectors.toSet());

        assertEquals(2, results.size());
        assertTrue(names.contains("Cordless Drill"));
    }
}
