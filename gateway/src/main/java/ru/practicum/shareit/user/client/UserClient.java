package ru.practicum.shareit.user.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder,
                      ObjectMapper objectMapper) {
        super(builder, objectMapper, serverUrl + API_PREFIX);
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> update(Long userId, UserDto userDto) {
        return patch("/" + userId, null, userDto);
    }

    public ResponseEntity<Object> getById(Long userId) {
        return get("/" + userId, null);
    }

    public ResponseEntity<Object> delete(Long userId) {
        return delete("/" + userId);
    }

    public ResponseEntity<Object> getAll() {
        return get("", null);
    }
}
