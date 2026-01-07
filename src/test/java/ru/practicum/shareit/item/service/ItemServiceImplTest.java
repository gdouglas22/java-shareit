package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_whenDtoNull_throwsValidationException() {
        assertThrows(ValidationException.class, () -> itemService.createItem(1L, null));
    }

    @Test
    void createItem_whenValid_returnsSaved() {
        User owner = User.builder().id(1L).name("Owner").email("o@mail.com").build();
        ItemDto request = ItemDto.builder()
                .name("Drill")
                .description("Cordless")
                .available(true)
                .build();
        Item saved = Item.builder()
                .id(10L)
                .name(request.getName())
                .description(request.getDescription())
                .available(true)
                .owner(owner)
                .build();
        when(userService.getUserEntity(owner.getId())).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        ItemDto result = itemService.createItem(owner.getId(), request);

        assertEquals(saved.getId(), result.getId());
        assertEquals("Drill", result.getName());
    }

    @Test
    void updateItem_whenNotOwner_throwsForbidden() {
        Item item = Item.builder()
                .id(3L)
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .owner(User.builder().id(1L).name("Owner").email("o@mail.com").build())
                .build();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(2L, item.getId(), ItemDto.builder().name("Update").build()));
    }

    @Test
    void getItem_whenOwner_returnsWithBookingsAndComments() {
        Long ownerId = 1L;
        Item item = Item.builder()
                .id(5L)
                .name("Hammer")
                .description("Heavy")
                .available(true)
                .owner(User.builder().id(ownerId).name("Owner").email("o@mail.com").build())
                .build();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(item.getId(), any(Sort.class))).thenReturn(List.of(
                CommentMapper.toComment(CommentCreateDto.builder().text("nice").build(), item,
                        User.builder().id(9L).name("Bob").email("b@mail.com").build(), LocalDateTime.now())
        ));
        when(bookingRepository.findFirstByItem_IdAndEndIsBeforeAndStatusOrderByEndDesc(any(), any(), any()))
                .thenReturn(Booking.builder().id(11L).booker(User.builder().id(4L).build()).build());
        when(bookingRepository.findFirstByItem_IdAndStartIsAfterAndStatusOrderByStartAsc(any(), any(), any()))
                .thenReturn(Booking.builder().id(12L).booker(User.builder().id(6L).build()).build());

        ItemResponseDto response = itemService.getItem(item.getId(), ownerId);

        assertNotNull(response.getLastBooking());
        assertNotNull(response.getNextBooking());
        assertEquals(1, response.getComments().size());
    }

    @Test
    void addComment_whenNoPastBooking_throwsValidationException() {
        Long userId = 2L;
        Long itemId = 8L;
        User author = User.builder().id(userId).name("User").email("u@mail.com").build();
        Item item = Item.builder().id(itemId).name("Book").description("Desc").available(true)
                .owner(User.builder().id(1L).build()).build();
        when(userService.getUserEntity(userId)).thenReturn(author);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByItem_IdAndBooker_IdAndEndIsBeforeAndStatus(itemId, userId, any(), BookingStatus.APPROVED))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(userId, itemId,
                CommentCreateDto.builder().text("text").build()));
    }
}
