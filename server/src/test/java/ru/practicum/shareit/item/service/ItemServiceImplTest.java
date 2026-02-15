package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.policy.ItemAccessPolicy;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validation.ItemValidator;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserLookupService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserLookupService userService;

    @Mock
    private ItemValidator itemValidator;

    @Mock
    private ItemAccessPolicy itemAccessPolicy;

    @Mock
    private CommentService commentService;

    @Mock
    private ItemResponseAssembler itemResponseAssembler;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_throwsWhenDtoNull() {
        assertThrows(ValidationException.class, () -> itemService.createItem(1L, null));
    }

    @Test
    void createItem_throwsWhenRequestMissing() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Cordless")
                .available(true)
                .requestId(10L)
                .build();
        when(userService.getUserEntity(1L)).thenReturn(User.builder().id(1L).build());
        when(itemRequestRepository.existsById(10L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, dto));
    }

    @Test
    void createItem_savesAndReturnsDto() {
        ItemDto dto = ItemDto.builder()
                .id(100L)
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build();
        User owner = User.builder().id(1L).name("Owner").email("owner@mail.com").build();
        when(userService.getUserEntity(1L)).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        ItemDto result = itemService.createItem(1L, dto);

        assertEquals(11L, result.getId());
        assertEquals("Drill", result.getName());
        verify(itemValidator).validateCreate(dto);
        verify(itemRequestRepository, never()).existsById(any());
    }

    @Test
    void updateItem_throwsWhenDtoNull() {
        assertThrows(ValidationException.class, () -> itemService.updateItem(1L, 2L, null));
    }

    @Test
    void updateItem_throwsWhenItemMissing() {
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        ItemDto patch = ItemDto.builder().name("New").build();
        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, 2L, patch));
    }

    @Test
    void updateItem_updatesOnlyProvidedFields() {
        Item existing = Item.builder()
                .id(2L)
                .name("Old")
                .description("Old desc")
                .available(true)
                .owner(User.builder().id(1L).build())
                .build();
        when(itemRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(existing)).thenReturn(existing);
        ItemDto patch = ItemDto.builder().name("New").available(false).build();

        ItemDto result = itemService.updateItem(1L, 2L, patch);

        assertEquals("New", result.getName());
        assertEquals("Old desc", result.getDescription());
        assertEquals(false, result.getAvailable());
        verify(itemAccessPolicy).checkOwner(existing, 1L);
        verify(itemValidator).validateUpdate(patch);
    }

    @Test
    void getItem_throwsWhenMissing() {
        when(itemRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(2L, 1L));
    }

    @Test
    void getItem_returnsAssemblerResult() {
        Item item = Item.builder().id(2L).build();
        ItemResponseDto response = ItemResponseDto.builder().id(2L).name("Item").build();
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(itemResponseAssembler.toItemResponse(item, 1L)).thenReturn(response);

        ItemResponseDto result = itemService.getItem(2L, 1L);

        assertEquals(2L, result.getId());
    }

    @Test
    void getItemsByOwner_returnsMappedList() {
        when(userService.getUserEntity(1L)).thenReturn(User.builder().id(1L).build());
        Item item = Item.builder().id(10L).name("Item").build();
        ItemOwnerListDto ownerListDto = ItemOwnerListDto.builder().id(10L).name("Item").build();
        when(itemRepository.findByOwner_Id(eq(1L), any(Sort.class))).thenReturn(List.of(item));
        when(itemResponseAssembler.toOwnerList(item)).thenReturn(ownerListDto);

        List<ItemOwnerListDto> result = itemService.getItemsByOwner(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void searchItems_returnsEmptyForBlankText() {
        List<ItemDto> result = itemService.searchItems(" ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void searchItems_returnsMappedItems() {
        Item item = Item.builder()
                .id(5L)
                .name("Hammer")
                .description("Heavy")
                .available(true)
                .build();
        when(itemRepository.searchAvailable("ham")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("ham");

        assertEquals(1, result.size());
        assertEquals("Hammer", result.get(0).getName());
    }

    @Test
    void addComment_delegatesToCommentService() {
        CommentCreateDto createDto = CommentCreateDto.builder().text("Nice").build();
        CommentDto response = CommentDto.builder().id(1L).text("Nice").build();
        when(commentService.addComment(1L, 2L, createDto)).thenReturn(response);

        CommentDto result = itemService.addComment(1L, 2L, createDto);

        assertEquals(1L, result.getId());
    }
}

