package com.ordersplatform.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired MockMvc mvc;
    @MockBean OrderEventPublisher publisher;

    @Test
    void createsOrderAndPublishesEvent() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerEmail\":\"a@b.com\",\"item\":\"laptop\",\"quantity\":1,\"price\":999.99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        verify(publisher, times(1)).publishOrderCreated(any());
    }

    @Test
    void rejectsBadOrder() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"item\":\"\",\"quantity\":0,\"price\":0}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(publisher);
    }
}
