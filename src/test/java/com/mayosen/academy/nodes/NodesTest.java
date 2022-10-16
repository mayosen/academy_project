package com.mayosen.academy.nodes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.FileCopyUtils;

import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NodesTest {
    private final MockMvc mockMvc;
    private final ResourceLoader resourceLoader;

    @Autowired
    public NodesTest(MockMvc mockMvc, ResourceLoader resourceLoader) {
        this.mockMvc = mockMvc;
        this.resourceLoader = resourceLoader;
    }

    private MockHttpServletRequestBuilder postRequest(String filename) throws Exception {
        String path = String.format("classpath:/imports/%s", filename);
        File file = resourceLoader.getResource(path).getFile();
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        return post("/imports").contentType(MediaType.APPLICATION_JSON).content(bytes);
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

    @Test
    @Sql("/truncate.sql")
    void sizes() throws Exception {
        mockMvc
                .perform(postRequest("group.json"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/nodes/a")).andExpect(jsonPath("$.size").value(410));
        mockMvc.perform(get("/nodes/f4")).andExpect(jsonPath("$.size").value(40));

        mockMvc.perform(get("/nodes/b2")).andExpect(jsonPath("$.size").value(0));

        mockMvc.perform(get("/nodes/b3")).andExpect(jsonPath("$.size").value(200));
        mockMvc.perform(get("/nodes/c1")).andExpect(jsonPath("$.size").value(200));
        mockMvc.perform(get("/nodes/f6")).andExpect(jsonPath("$.size").value(200));

        mockMvc.perform(get("/nodes/b1")).andExpect(jsonPath("$.size").value(210));
        mockMvc.perform(get("/nodes/f1")).andExpect(jsonPath("$.size").value(50));
        mockMvc.perform(get("/nodes/f2")).andExpect(jsonPath("$.size").value(60));
        mockMvc.perform(get("/nodes/c2")).andExpect(jsonPath("$.size").value(100));
        mockMvc.perform(get("/nodes/f3")).andExpect(jsonPath("$.size").value(100));
    }
}
