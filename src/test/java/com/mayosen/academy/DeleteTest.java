package com.mayosen.academy;

import com.mayosen.academy.domain.ItemType;
import com.mayosen.academy.requests.ItemImport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;

import static com.mayosen.academy.Utils.postRequest;
import static com.mayosen.academy.Utils.requestOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeleteTest {
    private final MockMvc mockMvc;

    @Autowired
    public DeleteTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private MockHttpServletRequestBuilder deleteRequest(String id, String date) {
        return delete(String.format("/delete/%s?date=%s", id, date));
    }

    private MockHttpServletRequestBuilder deleteRequest(String id) {
        return deleteRequest(id, Instant.now().toString());
    }

    @Test
    void noDateParam() throws Exception {
        mockMvc
                .perform(delete("/delete/notexisting"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void notFound() throws Exception {
        mockMvc
                .perform(deleteRequest("notexisting"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Item not found"));
    }

    @Test
    void deleteForever() throws Exception {
        ItemImport item = new ItemImport("file", "url", null, ItemType.FILE, 100L);
        mockMvc.perform(postRequest(requestOf(item))).andExpect(status().isOk());
        mockMvc.perform(deleteRequest("file")).andExpect(status().isOk());
        mockMvc.perform(get("/nodes/file")).andExpect(status().isNotFound());
        mockMvc.perform(deleteRequest("file")).andExpect(status().isNotFound());
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/importGroup.sql"})
    void updateParents() throws Exception {
        String date = "2022-11-11T12:00:00.000Z";
        mockMvc.perform(deleteRequest("f3", date)).andExpect(status().isOk());

        String expectedDate = "2022-11-11T12:00:00Z";
        mockMvc.perform(get("/nodes/c2"))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc.perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.size").value(110))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc.perform(get("/nodes/a"))
                .andExpect(jsonPath("$.size").value(310))
                .andExpect(jsonPath("$.date").value(expectedDate));

        mockMvc.perform(get("/nodes/f1"))
                .andExpect(jsonPath("$.date").value("2022-09-11T12:00:00Z"));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void deleteItemWithBlankId() throws Exception {
        ItemImport item = new ItemImport("", null, null, ItemType.FOLDER, null);
        mockMvc.perform(postRequest(requestOf(item))).andExpect(status().isOk());
        mockMvc.perform(deleteRequest("")).andExpect(status().isOk());
        mockMvc.perform(get("/nodes/")).andExpect(status().isNotFound());
    }
}
