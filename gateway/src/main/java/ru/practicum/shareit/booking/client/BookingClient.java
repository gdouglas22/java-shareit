package ru.practicum.shareit.booking.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl,
                         RestTemplateBuilder builder,
                         ObjectMapper objectMapper) {
        super(builder, objectMapper, serverUrl + API_PREFIX);
    }

    public ResponseEntity<Object> create(Long userId, BookingCreateRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> approve(Long ownerId, Long bookingId, boolean approved) {
        return patch("/" + bookingId + "?approved={approved}", ownerId, null, Map.of("approved", approved));
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookings(Long userId, String state) {
        return get("?state={state}", userId, Map.of("state", state));
    }

    public ResponseEntity<Object> getOwnerBookings(Long ownerId, String state) {
        return get("/owner?state={state}", ownerId, Map.of("state", state));
    }
}
