package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void create_returnsCreatedUser() throws Exception {
        UserDto request = UserDto.builder().name("User").email("user@mail.com").build();
        UserDto response = UserDto.builder().id(1L).name("User").email("user@mail.com").build();
        when(userService.createUser(eq(request))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void update_returnsUpdatedUser() throws Exception {
        UserDto request = UserDto.builder().name("New").build();
        UserDto response = UserDto.builder().id(2L).name("New").email("mail@mail.com").build();
        when(userService.updateUser(2L, request)).thenReturn(response);

        mockMvc.perform(patch("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void getById_returnsUser() throws Exception {
        when(userService.getUser(3L)).thenReturn(UserDto.builder().id(3L).name("Name").email("n@mail.com").build());

        mockMvc.perform(get("/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.email").value("n@mail.com"));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(4L);
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                UserDto.builder().id(1L).name("A").email("a@mail.com").build(),
                UserDto.builder().id(2L).name("B").email("b@mail.com").build()
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].name").value("B"));
    }
}

