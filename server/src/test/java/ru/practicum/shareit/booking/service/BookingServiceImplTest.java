package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.policy.BookingAccessPolicy;
import ru.practicum.shareit.booking.policy.BookingCreationPolicy;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.state.BookingStateResolver;
import ru.practicum.shareit.booking.state.BookingStateStrategy;
import ru.practicum.shareit.booking.state.BookingStateStrategyProvider;
import ru.practicum.shareit.booking.validation.BookingValidator;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserLookupService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserLookupService userService;

    @Mock
    private Clock clock;

    @Mock
    private BookingStateResolver bookingStateResolver;

    @Mock
    private BookingStateStrategyProvider bookingStateStrategyProvider;

    @Mock
    private BookingValidator bookingValidator;

    @Mock
    private BookingAccessPolicy bookingAccessPolicy;

    @Mock
    private BookingCreationPolicy bookingCreationPolicy;

    @Mock
    private BookingStateStrategy bookingStateStrategy;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_throwsWhenRequestNull() {
        assertThrows(ValidationException.class, () -> bookingService.createBooking(1L, null));
    }

    @Test
    void createBooking_throwsWhenItemNotFound() {
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(5L)
                .start(LocalDateTime.of(2026, 2, 14, 10, 0))
                .end(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build();
        when(userService.getUserEntity(1L)).thenReturn(User.builder().id(1L).build());
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(1L, dto));
    }

    @Test
    void createBooking_savesAndReturnsDto() {
        User booker = User.builder().id(1L).name("Booker").build();
        Item item = Item.builder().id(5L).name("Item").owner(User.builder().id(2L).build()).available(true).build();
        BookingCreateRequestDto dto = BookingCreateRequestDto.builder()
                .itemId(5L)
                .start(LocalDateTime.of(2026, 2, 14, 10, 0))
                .end(LocalDateTime.of(2026, 2, 14, 12, 0))
                .build();
        when(userService.getUserEntity(1L)).thenReturn(booker);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        BookingResponseDto response = bookingService.createBooking(1L, dto);

        assertEquals(99L, response.getId());
        assertEquals(BookingStatus.WAITING, response.getStatus());
        verify(bookingValidator).validateCreateRequest(dto);
        verify(bookingCreationPolicy).checkCanBook(1L, item);
    }

    @Test
    void approveBooking_throwsWhenMissing() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(1L, 10L, true));
    }

    @Test
    void approveBooking_setsApprovedStatus() {
        Booking booking = Booking.builder().id(10L).status(BookingStatus.WAITING).build();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto response = bookingService.approveBooking(2L, 10L, true);

        assertEquals(BookingStatus.APPROVED, response.getStatus());
        verify(bookingAccessPolicy).checkCanApprove(2L, booking);
        verify(bookingValidator).validateApproval(booking);
    }

    @Test
    void approveBooking_setsRejectedStatus() {
        Booking booking = Booking.builder().id(10L).status(BookingStatus.WAITING).build();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto response = bookingService.approveBooking(2L, 10L, false);

        assertEquals(BookingStatus.REJECTED, response.getStatus());
    }

    @Test
    void getBooking_checksAccessAndReturnsDto() {
        Booking booking = Booking.builder()
                .id(10L)
                .item(Item.builder().id(5L).name("Item").build())
                .booker(User.builder().id(1L).name("Booker").build())
                .status(BookingStatus.WAITING)
                .build();
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        BookingResponseDto response = bookingService.getBooking(1L, 10L);

        assertEquals(10L, response.getId());
        verify(bookingAccessPolicy).checkCanView(1L, booking);
    }

    @Test
    void getBookings_resolvesStateAndUsesStrategy() {
        User user = User.builder().id(1L).build();
        Booking booking = Booking.builder()
                .id(1L)
                .item(Item.builder().id(2L).name("Item").build())
                .booker(User.builder().id(1L).name("Booker").build())
                .status(BookingStatus.WAITING)
                .build();
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);
        when(userService.getUserEntity(1L)).thenReturn(user);
        when(bookingStateResolver.resolve("ALL")).thenReturn(BookingState.ALL);
        when(bookingStateStrategyProvider.getStrategy(BookingState.ALL)).thenReturn(bookingStateStrategy);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-14T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(bookingStateStrategy.findByBooker(eq(1L), any(Sort.class), eq(now))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookings(1L, "ALL");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getOwnerBookings_resolvesStateAndUsesStrategy() {
        User owner = User.builder().id(2L).build();
        Booking booking = Booking.builder()
                .id(7L)
                .item(Item.builder().id(2L).name("Item").build())
                .booker(User.builder().id(1L).name("Booker").build())
                .status(BookingStatus.APPROVED)
                .build();
        LocalDateTime now = LocalDateTime.of(2026, 2, 14, 12, 0);
        when(userService.getUserEntity(2L)).thenReturn(owner);
        when(bookingStateResolver.resolve("WAITING")).thenReturn(BookingState.WAITING);
        when(bookingStateStrategyProvider.getStrategy(BookingState.WAITING)).thenReturn(bookingStateStrategy);
        when(clock.instant()).thenReturn(Instant.parse("2026-02-14T12:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(bookingStateStrategy.findByOwner(eq(2L), any(Sort.class), eq(now))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(2L, "WAITING");

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getId());
    }
}

