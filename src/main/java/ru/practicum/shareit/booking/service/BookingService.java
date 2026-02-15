package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingCreateRequestDto requestDto);

    BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved);

    BookingResponseDto getBooking(Long userId, Long bookingId);

    List<BookingResponseDto> getBookings(Long bookerId, String state);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, String state);
}
