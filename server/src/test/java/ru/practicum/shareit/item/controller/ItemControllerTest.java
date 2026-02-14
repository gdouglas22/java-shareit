package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemOwnerListDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_returnsCreatedItem() throws Exception {
        ItemDto request = ItemDto.builder().name("Drill").description("Cordless").available(true).build();
        ItemDto response = ItemDto.builder().id(1L).name("Drill").description("Cordless").available(true).build();
        when(itemService.createItem(eq(10L), eq(request))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void update_returnsUpdatedItem() throws Exception {
        ItemDto request = ItemDto.builder().name("New").build();
        ItemDto response = ItemDto.builder().id(2L).name("New").description("Desc").available(true).build();
        when(itemService.updateItem(10L, 2L, request)).thenReturn(response);

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void getById_returnsItemResponse() throws Exception {
        ItemResponseDto response = ItemResponseDto.builder().id(3L).name("Item").build();
        when(itemService.getItem(3L, 10L)).thenReturn(response);

        mockMvc.perform(get("/items/3")
                        .header("X-Sharer-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void getOwnerItems_returnsList() throws Exception {
        when(itemService.getItemsByOwner(10L)).thenReturn(List.of(ItemOwnerListDto.builder().id(4L).name("Item").build()));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
    }

    @Test
    void search_delegatesByText() throws Exception {
        when(itemService.searchItems("drill")).thenReturn(List.of(ItemDto.builder().id(5L).name("Drill").build()));

        mockMvc.perform(get("/items/search")
                        .queryParam("text", "drill")
                        .header("X-Sharer-User-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        verify(itemService).searchItems("drill");
    }

    @Test
    void addComment_returnsCreatedComment() throws Exception {
        CommentCreateDto request = CommentCreateDto.builder().text("Nice").build();
        CommentDto response = CommentDto.builder().id(6L).text("Nice").authorName("User").build();
        when(itemService.addComment(10L, 2L, request)).thenReturn(response);

        mockMvc.perform(post("/items/2/comment")
                        .header("X-Sharer-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.authorName").value("User"));
    }
}

