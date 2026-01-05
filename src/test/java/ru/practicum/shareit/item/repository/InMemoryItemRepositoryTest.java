package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryItemRepositoryTest {

    private InMemoryItemRepository repository;
    private User owner;

    @BeforeEach
    void setUp() {
        repository = new InMemoryItemRepository();
        owner = User.builder().id(1L).name("Owner").email("owner@mail.com").build();
    }

    @Test
    void save_assignsId() {
        Item item = Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build();

        Item saved = repository.save(item);

        assertNotNull(saved.getId());
        assertEquals(saved.getId(), repository.findById(saved.getId()).orElseThrow().getId());
    }

    @Test
    void findAllByOwnerId_whenNull_returnsEmptyList() {
        assertTrue(repository.findAllByOwnerId(null).isEmpty());
    }

    @Test
    void findAllByOwnerId_filtersByOwner() {
        repository.save(Item.builder()
                .name("Mine")
                .description("Owned")
                .available(true)
                .owner(owner)
                .build());
        repository.save(Item.builder()
                .name("Other")
                .description("Not owned")
                .available(true)
                .owner(User.builder().id(2L).name("Other").email("other@mail.com").build())
                .build());

        List<Item> items = repository.findAllByOwnerId(owner.getId());

        assertEquals(1, items.size());
        assertEquals("Mine", items.get(0).getName());
    }
}
