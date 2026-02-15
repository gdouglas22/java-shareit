package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRequestModelTest {

    @Test
    void builder_setsAllFields() {
        User requestor = User.builder().id(1L).name("Requester").email("requester@mail.com").build();
        LocalDateTime created = LocalDateTime.now();

        ItemRequest request = ItemRequest.builder()
                .id(2L)
                .description("Need a drill")
                .requestor(requestor)
                .created(created)
                .build();

        assertEquals(2L, request.getId());
        assertEquals("Need a drill", request.getDescription());
        assertEquals(requestor, request.getRequestor());
        assertEquals(created, request.getCreated());
    }
}
