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
import java.util.List;

import static com.mayosen.academy.Utils.postRequest;
import static com.mayosen.academy.Utils.requestOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UpdatesTest {
    private final MockMvc mockMvc;

    @Autowired
    public UpdatesTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private MockHttpServletRequestBuilder getRequest(String date) {
        return get(String.format("/updates?date=%s", date));
    }

    private MockHttpServletRequestBuilder getRequest() {
        return getRequest(Instant.now().toString());
    }

    @Test
    void noDateParam() throws Exception {
        mockMvc
                .perform(get("/updates"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void doNotGetFolders() throws Exception {
        List<ItemImport> items = List.of(
                new ItemImport("first", null, null, ItemType.FOLDER, null),
                new ItemImport("second", null, null, ItemType.FOLDER, null)
        );
        mockMvc.perform(postRequest(requestOf(items))).andExpect(status().isOk());
        mockMvc
                .perform(getRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void getOnlyFiles() throws Exception {
        List<ItemImport> items = List.of(
                new ItemImport("first", null, null, ItemType.FOLDER, null),
                new ItemImport("second", null, null, ItemType.FOLDER, null),
                new ItemImport("file", "url", null, ItemType.FILE, 40L)
        );
        mockMvc.perform(postRequest(requestOf(items))).andExpect(status().isOk());
        mockMvc
                .perform(getRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value("file"));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void filterFilesByDateInterval() throws Exception {
        ItemImport oldItem = new ItemImport("file", "url", null, ItemType.FILE, 100L);
        mockMvc
                .perform(postRequest(requestOf(oldItem, Instant.parse("2022-09-11T11:00:00Z"))))
                .andExpect(status().isOk());

        List<ItemImport> items = List.of(
                new ItemImport("folder", null, null, ItemType.FOLDER, null),
                new ItemImport("file", "url", null, ItemType.FILE, 40L)
        );
        mockMvc
                .perform(postRequest(requestOf(items, Instant.parse("2022-09-11T12:00:00Z"))))
                .andExpect(status().isOk());

        ItemImport item3 = new ItemImport("file2", "url", null, ItemType.FILE, 20L);
        mockMvc
                .perform(postRequest(requestOf(item3, Instant.parse("2022-09-11T12:20:00Z"))))
                .andExpect(status().isOk());

        mockMvc
                .perform(getRequest("2022-09-12T12:00:00.000Z"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[*].id", containsInAnyOrder("file", "file2")));
    }
}
