package com.mayosen.academy.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayosen.academy.domain.ItemType;
import com.mayosen.academy.requests.ItemImport;
import com.mayosen.academy.requests.ItemImportRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static com.mayosen.academy.imports.Utils.requestOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImportCorrectnessTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImportCorrectnessTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private MockHttpServletRequestBuilder postRequest(ItemImportRequest request) throws JsonProcessingException {
        byte[] bytes = objectMapper.writeValueAsBytes(request);
        return post("/imports").contentType(MediaType.APPLICATION_JSON).content(bytes);
    }

    @Test
    @Sql("/truncate.sql")
    void updateDateWithoutContentChanges() throws Exception {
        List<ItemImport> items = List.of(
                new ItemImport("parent", null, null, ItemType.FOLDER, null),
                new ItemImport("child", null, "parent", ItemType.FOLDER, null)
        );

        mockMvc
                .perform(postRequest(requestOf(items, Instant.parse("2022-10-10T00:00:00Z"))))
                .andExpect(status().isOk());
        // Заодно проверка того, что дата возвращается всегда с точностью до миллисекунд
        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value("2022-10-10T00:00:00.000Z"));
        mockMvc
                .perform(get("/nodes/child"))
                .andExpect(jsonPath("$.date").value("2022-10-10T00:00:00.000Z"));

        mockMvc
                .perform(postRequest(requestOf(items, Instant.parse("2022-10-15T00:00:00Z"))))
                .andExpect(status().isOk());
        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value("2022-10-15T00:00:00.000Z"));
        mockMvc
                .perform(get("/nodes/child"))
                .andExpect(jsonPath("$.date").value("2022-10-15T00:00:00.000Z"));
    }

    @Test
    @Sql("/truncate.sql")
    void updateParentDate() throws Exception {
        ItemImport parent = new ItemImport("parent", null, null, ItemType.FOLDER, null);
        ItemImport child = new ItemImport("child", null, "parent", ItemType.FOLDER, null);

        mockMvc
                .perform(postRequest(requestOf(List.of(parent, child), Instant.parse("2022-10-10T00:00:00.000Z"))))
                .andExpect(status().isOk());
        mockMvc
                .perform(postRequest(requestOf(child, Instant.parse("2022-10-15T00:00:00.000Z"))))
                .andExpect(status().isOk());

        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value("2022-10-15T00:00:00.000Z"));
        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value("2022-10-15T00:00:00.000Z"));
    }
}
