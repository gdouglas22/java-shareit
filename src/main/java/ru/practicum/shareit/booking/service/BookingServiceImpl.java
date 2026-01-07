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
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingCreateRequestDto requestDto) {
        if (requestDto == null) {
            throw new ValidationException("нужно передать данные бронирования");
        }
        if (requestDto.getItemId() == null) {
            throw new ValidationException("нужно указать вещь для бронирования");
        }
        validateDates(requestDto);

        User booker = userService.getUserEntity(userId);
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("вещь с id=" + requestDto.getItemId() + " не найдена"));

        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("нельзя бронировать свою вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("вещь недоступна для бронирования");
        }

        Booking booking = BookingMapper.toBooking(requestDto, item, booker, BookingStatus.WAITING);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("бронирование с id=" + bookingId + " не найдено"));
        if (booking.getItem() == null || booking.getItem().getOwner() == null
                || !booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() == BookingStatus.APPROVED || booking.getStatus() == BookingStatus.REJECTED) {
            throw new ValidationException("статус уже изменён");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    public BookingResponseDto getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("бронирование с id=" + bookingId + " не найдено"));
        Long ownerId = booking.getItem() != null && booking.getItem().getOwner() != null
                ? booking.getItem().getOwner().getId()
                : null;
        if (!booking.getBooker().getId().equals(userId) && !userId.equals(ownerId)) {
            throw new NotFoundException("бронирование не найдено для пользователя");
        }
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookings(Long bookerId, String state) {
        userService.getUserEntity(bookerId);
        BookingState bookingState = parseState(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(bookerId, now, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findByBooker_IdAndEndIsBefore(bookerId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBooker_IdAndStartIsAfter(bookerId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBooker_IdAndStatus(bookerId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBooker_IdAndStatus(bookerId, BookingStatus.REJECTED, sort);
                break;
            case ALL:
            default:
                bookings = bookingRepository.findByBooker_Id(bookerId, sort);
                break;
        }
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        userService.getUserEntity(ownerId);
        BookingState bookingState = parseState(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(ownerId, now, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findByItem_Owner_IdAndEndIsBefore(ownerId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsAfter(ownerId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(ownerId, BookingStatus.REJECTED, sort);
                break;
            case ALL:
            default:
                bookings = bookingRepository.findByItem_Owner_Id(ownerId, sort);
                break;
        }
        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    private BookingState parseState(String state) {
        String value = state == null ? BookingState.ALL.name() : state.toUpperCase(Locale.ROOT);
        try {
            return BookingState.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Unknown state: " + state);
        }
    }

    private void validateDates(BookingCreateRequestDto dto) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("нужно указать даты бронирования");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("дата окончания должна быть позже начала");
        }
    }
}
