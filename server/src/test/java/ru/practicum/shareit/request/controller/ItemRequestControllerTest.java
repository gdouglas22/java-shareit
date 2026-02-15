package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create_returnsCreatedRequest() throws Exception {
        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.of(2026, 2, 10, 12, 0))
                .items(List.of())
                .build();
        when(itemRequestService.create(eq(10L), eq(createDto))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getOwnRequests_returnsList() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(5L)
                .description("Need a bike")
                .created(LocalDateTime.of(2026, 2, 10, 13, 0))
                .items(List.of(ItemRequestItemDto.builder().id(9L).name("Bike").ownerId(3L).build()))
                .build();
        when(itemRequestService.getOwnRequests(1L)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].items[0].name").value("Bike"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(3));
    }

    @Test
    void getOtherUsersRequests_passesPagination() throws Exception {
        when(itemRequestService.getOtherUsersRequests(2L, 5, 10)).thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2L)
                        .queryParam("from", "5")
                        .queryParam("size", "10"))
                .andExpect(status().isOk());

        verify(itemRequestService).getOtherUsersRequests(2L, 5, 10);
    }

    @Test
    void getById_returnsRequest() throws Exception {
        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(7L)
                .description("Need a ladder")
                .created(LocalDateTime.of(2026, 2, 10, 14, 0))
                .items(List.of())
                .build();
        when(itemRequestService.getById(4L, 7L)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/7")
                        .header("X-Sharer-User-Id", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.description").value("Need a ladder"));
    }
}
