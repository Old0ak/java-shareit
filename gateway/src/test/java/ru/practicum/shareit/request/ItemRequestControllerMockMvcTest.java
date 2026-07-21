package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestPostDto;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void shouldCreateRequestSuccessfully() throws Exception {
        ItemRequestPostDto postDto = new ItemRequestPostDto("Нужен шуруповерт");
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);

        Mockito.when(itemRequestClient.createRequest(any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFindAllByRequestor() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

        Mockito.when(itemRequestClient.findAllByRequestor(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFindAllRequestsWithPagination() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

        Mockito.when(itemRequestClient.findAllByRequestor(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFindRequestById() throws Exception {
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(HttpStatus.OK);

        Mockito.when(itemRequestClient.findAllByRequestor(any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}
