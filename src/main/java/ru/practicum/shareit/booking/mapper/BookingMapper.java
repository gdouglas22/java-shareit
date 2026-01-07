package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.ItemShortDto;
import ru.practicum.shareit.booking.dto.UserShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static Booking toBooking(BookingCreateRequestDto dto, Item item, User booker, BookingStatus status) {
        if (dto == null) {
            return null;
        }
        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(status)
                .build();
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(toItemShort(booking.getItem()))
                .booker(toUserShort(booking.getBooker()))
                .build();
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    private static ItemShortDto toItemShort(Item item) {
        if (item == null) {
            return null;
        }
        return ItemShortDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }

    private static UserShortDto toUserShort(User user) {
        if (user == null) {
            return null;
        }
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
