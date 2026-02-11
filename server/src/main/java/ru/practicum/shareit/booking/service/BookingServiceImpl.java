package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserLookupService userService;
    private final Clock clock;
    private final BookingStateResolver bookingStateResolver;
    private final BookingStateStrategyProvider bookingStateStrategyProvider;
    private final BookingValidator bookingValidator;
    private final BookingAccessPolicy bookingAccessPolicy;
    private final BookingCreationPolicy bookingCreationPolicy;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingCreateRequestDto requestDto) {
        if (requestDto == null) {
            throw new ValidationException("нужно передать данные бронирования");
        }
        bookingValidator.validateCreateRequest(requestDto);

        User booker = userService.getUserEntity(userId);
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("вещь с id=" + requestDto.getItemId() + " не найдена"));
        bookingCreationPolicy.checkCanBook(userId, item);

        Booking booking = BookingMapper.toBooking(requestDto, item, booker, BookingStatus.WAITING);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("бронирование с id=" + bookingId + " не найдено"));
        bookingAccessPolicy.checkCanApprove(ownerId, booking);
        bookingValidator.validateApproval(booking);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("бронирование с id=" + bookingId + " не найдено"));
        bookingAccessPolicy.checkCanView(userId, booking);
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookings(Long bookerId, String state) {
        userService.getUserEntity(bookerId);
        BookingState bookingState = bookingStateResolver.resolve(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now(clock);
        BookingStateStrategy strategy = bookingStateStrategyProvider.getStrategy(bookingState);
        List<Booking> bookings = strategy.findByBooker(bookerId, sort, now);
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        userService.getUserEntity(ownerId);
        BookingState bookingState = bookingStateResolver.resolve(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now(clock);
        BookingStateStrategy strategy = bookingStateStrategyProvider.getStrategy(bookingState);
        List<Booking> bookings = strategy.findByOwner(ownerId, sort, now);
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

}
