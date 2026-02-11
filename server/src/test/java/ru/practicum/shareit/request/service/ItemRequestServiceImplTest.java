package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserLookupService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserLookupService userService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Test
    void create_savesRequestWithTimestamp() {
        User user = User.builder().id(1L).name("User").email("user@mail.com").build();
        Instant now = Instant.parse("2026-02-10T15:00:00Z");
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder().description("Need a drill").build();

        when(userService.getUserEntity(1L)).thenReturn(user);
        when(clock.instant()).thenReturn(now);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest request = invocation.getArgument(0);
            request.setId(10L);
            return request;
        });

        ItemRequestResponseDto response = itemRequestService.create(1L, dto);

        assertEquals(10L, response.getId());
        assertEquals("Need a drill", response.getDescription());
        assertEquals(LocalDateTime.of(2026, 2, 10, 15, 0), response.getCreated());
    }

    @Test
    void create_throwsWhenDescriptionBlank() {
        ItemRequestCreateDto dto = ItemRequestCreateDto.builder().description(" ").build();

        assertThrows(ValidationException.class, () -> itemRequestService.create(1L, dto));
    }

    @Test
    void getOwnRequests_returnsRequestsWithItems() {
        User owner = User.builder().id(3L).name("Owner").email("owner@mail.com").build();
        ItemRequest request = ItemRequest.builder()
                .id(100L)
                .description("Need a bike")
                .requestor(owner)
                .created(LocalDateTime.of(2026, 2, 10, 12, 0))
                .build();
        Item item = Item.builder()
                .id(200L)
                .name("Bike")
                .owner(owner)
                .requestId(100L)
                .build();

        when(userService.getUserEntity(3L)).thenReturn(owner);
        when(itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(3L)).thenReturn(List.of(request));
        when(itemRepository.findByRequestIdIn(eq(Set.of(100L)), any(Sort.class))).thenReturn(List.of(item));

        List<ItemRequestResponseDto> response = itemRequestService.getOwnRequests(3L);

        assertEquals(1, response.size());
        assertEquals(100L, response.get(0).getId());
        assertEquals(1, response.get(0).getItems().size());
        assertEquals(200L, response.get(0).getItems().get(0).getId());
        assertEquals(3L, response.get(0).getItems().get(0).getOwnerId());
    }

    @Test
    void getOtherUsersRequests_throwsWhenInvalidPagination() {
        when(userService.getUserEntity(1L)).thenReturn(User.builder().id(1L).build());

        assertThrows(ValidationException.class, () -> itemRequestService.getOtherUsersRequests(1L, -1, 10));
        assertThrows(ValidationException.class, () -> itemRequestService.getOtherUsersRequests(1L, 0, 0));
    }

    @Test
    void getOtherUsersRequests_usesDefaultPagination() {
        when(userService.getUserEntity(2L)).thenReturn(User.builder().id(2L).build());
        when(itemRequestRepository.findByRequestor_IdNot(eq(2L), any(Pageable.class))).thenReturn(List.of());

        itemRequestService.getOtherUsersRequests(2L, null, null);

        verify(itemRequestRepository).findByRequestor_IdNot(eq(2L), any(Pageable.class));
    }
}
