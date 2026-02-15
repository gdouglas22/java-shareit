package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultItemResponseAssemblerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentService commentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private DefaultItemResponseAssembler assembler;

    @Test
    void toItemResponse_forNonOwnerReturnsWithoutBookings() {
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .owner(User.builder().id(10L).build())
                .build();
        when(commentService.getItemComments(1L)).thenReturn(List.of(CommentDto.builder().id(1L).text("Text").build()));

        ItemResponseDto response = assembler.toItemResponse(item, 20L);

        assertEquals(1L, response.getId());
        assertNull(response.getLastBooking());
        assertNull(response.getNextBooking());
        assertEquals(1, response.getComments().size());
    }

    @Test
    void toItemResponse_forOwnerIncludesLastAndNextBooking() {
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .owner(User.builder().id(10L).build())
                .build();
        Booking lastBooking = Booking.builder()
                .id(5L)
                .booker(User.builder().id(100L).build())
                .start(LocalDateTime.of(2026, 2, 10, 10, 0))
                .end(LocalDateTime.of(2026, 2, 10, 12, 0))
                .build();
        Booking nextBooking = Booking.builder()
                .id(6L)
                .booker(User.builder().id(101L).build())
                .start(LocalDateTime.of(2026, 2, 20, 10, 0))
                .end(LocalDateTime.of(2026, 2, 20, 12, 0))
                .build();
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-14T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(commentService.getItemComments(1L)).thenReturn(List.of());
        when(bookingRepository.findFirstByItem_IdAndEndIsBeforeAndStatusOrderByEndDesc(1L, now, BookingStatus.APPROVED))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstByItem_IdAndStartIsAfterAndStatusOrderByStartAsc(1L, now, BookingStatus.APPROVED))
                .thenReturn(nextBooking);

        ItemResponseDto response = assembler.toItemResponse(item, 10L);

        assertEquals(5L, response.getLastBooking().getId());
        assertEquals(6L, response.getNextBooking().getId());
    }

    @Test
    void toOwnerList_includesBookingsAndComments() {
        Item item = Item.builder().id(2L).name("Owner item").build();
        Booking lastBooking = Booking.builder().id(7L).booker(User.builder().id(1L).build()).build();
        Booking nextBooking = Booking.builder().id(8L).booker(User.builder().id(2L).build()).build();
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-14T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(bookingRepository.findFirstByItem_IdAndEndIsBeforeAndStatusOrderByEndDesc(2L, now, BookingStatus.APPROVED))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstByItem_IdAndStartIsAfterAndStatusOrderByStartAsc(2L, now, BookingStatus.APPROVED))
                .thenReturn(nextBooking);
        when(commentService.getItemComments(2L)).thenReturn(List.of(CommentDto.builder().id(1L).text("Text").build()));

        ItemOwnerListDto response = assembler.toOwnerList(item);

        assertEquals(2L, response.getId());
        assertEquals(7L, response.getLastBooking().getId());
        assertEquals(1, response.getComments().size());
    }
}

