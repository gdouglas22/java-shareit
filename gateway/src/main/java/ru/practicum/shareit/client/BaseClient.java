package ru.practicum.shareit.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.HeaderConstants;

import java.util.Map;

public abstract class BaseClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    protected BaseClient(RestTemplateBuilder builder, ObjectMapper objectMapper, String serverUrl) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .errorHandler(new DefaultResponseErrorHandler())
                .build();
        this.objectMapper = objectMapper;
    }

    protected ResponseEntity<Object> get(String path, Long userId) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, null, parameters);
    }

    protected ResponseEntity<Object> post(String path, Long userId, Object body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, body, null);
    }

    protected ResponseEntity<Object> post(String path, Object body) {
        return makeAndSendRequest(HttpMethod.POST, path, null, body, null);
    }

    protected ResponseEntity<Object> patch(String path, Long userId, Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, body, null);
    }

    protected ResponseEntity<Object> patch(String path, Long userId, Object body, Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, body, parameters);
    }

    protected ResponseEntity<Object> delete(String path) {
        return makeAndSendRequest(HttpMethod.DELETE, path, null, null, null);
    }

    protected ResponseEntity<Object> delete(String path, Long userId) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, null, null);
    }

    private ResponseEntity<Object> makeAndSendRequest(HttpMethod method,
                                                      String path,
                                                      Long userId,
                                                      Object body,
                                                      Map<String, Object> parameters) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, buildHeaders(userId));
        try {
            if (parameters == null) {
                return restTemplate.exchange(path, method, requestEntity, Object.class);
            }
            return restTemplate.exchange(path, method, requestEntity, Object.class, parameters);
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(parseBody(ex.getResponseBodyAsString()));
        }
    }

    private HttpHeaders buildHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
        if (userId != null) {
            headers.add(HeaderConstants.USER_ID, userId.toString());
        }
        return headers;
    }

    private Object parseBody(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, Object.class);
        } catch (JsonProcessingException ex) {
            return body;
        }
    }
}
