package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultItemResponseAssembler implements ItemResponseAssembler {

    private final BookingRepository bookingRepository;
    private final CommentService commentService;
    private final Clock clock;

    @Override
    public ItemResponseDto toItemResponse(Item item, Long requesterId) {
        List<CommentDto> comments = commentService.getItemComments(item.getId());
        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        if (isOwner(item, requesterId)) {
            LocalDateTime now = LocalDateTime.now(clock);
            lastBooking = BookingMapper.toBookingShortDto(
                    bookingRepository.findFirstByItem_IdAndEndIsBeforeAndStatusOrderByEndDesc(item.getId(), now, BookingStatus.APPROVED));
            nextBooking = BookingMapper.toBookingShortDto(
                    bookingRepository.findFirstByItem_IdAndStartIsAfterAndStatusOrderByStartAsc(item.getId(), now, BookingStatus.APPROVED));
        }
        return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public ItemOwnerListDto toOwnerList(Item item) {
        LocalDateTime now = LocalDateTime.now(clock);
        BookingShortDto lastBooking = BookingMapper.toBookingShortDto(
                bookingRepository.findFirstByItem_IdAndEndIsBeforeAndStatusOrderByEndDesc(item.getId(), now, BookingStatus.APPROVED));
        BookingShortDto nextBooking = BookingMapper.toBookingShortDto(
                bookingRepository.findFirstByItem_IdAndStartIsAfterAndStatusOrderByStartAsc(item.getId(), now, BookingStatus.APPROVED));
        List<CommentDto> comments = commentService.getItemComments(item.getId());
        return ItemMapper.toItemOwnerListDto(item, lastBooking, nextBooking, comments);
    }

    private boolean isOwner(Item item, Long requesterId) {
        return requesterId != null && item.getOwner() != null && requesterId.equals(item.getOwner().getId());
    }
}
