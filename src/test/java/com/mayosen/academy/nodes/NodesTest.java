package com.mayosen.academy.nodes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NodesTest {
    private final MockMvc mockMvc;

    @Autowired
    public NodesTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @Sql("/truncate.sql")
    void notFound() throws Exception {
        mockMvc
                .perform(get("/nodes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Item not found"));
    }

}
