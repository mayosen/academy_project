package com.mayosen.academy;

import com.mayosen.academy.domain.ItemType;
import com.mayosen.academy.requests.ItemImport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

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
class HistoryTest {
    private final MockMvc mockMvc;

    @Autowired
    public HistoryTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @Sql("/sql/truncate.sql")
    void itemWithBlankId() throws Exception {
        String date = "2022-11-11T12:00:00.020Z";
        ItemImport item = new ItemImport("", null, null, ItemType.FOLDER, null);
        mockMvc.perform(postRequest(requestOf(item, Instant.parse(date)))).andExpect(status().isOk());
        mockMvc
                .perform(get("/node//history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].date").value(date));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void AreUpdatesSaved() throws Exception {
        ItemImport parent = new ItemImport("parent", null, null, ItemType.FOLDER, null);
        ItemImport file = new ItemImport("file", "url", "parent", ItemType.FILE, 77L);

        String createDate = "2022-10-10T12:00:00Z";
        mockMvc
                .perform(postRequest(requestOf(List.of(parent, file), Instant.parse(createDate))))
                .andExpect(status().isOk());
        mockMvc
                .perform(get("/node/file/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].date").value(createDate))
                .andExpect(jsonPath("$.items[0].size").value(77));

        String updateDate = "2022-10-10T13:00:00Z";
        file.setSize(87L);
        mockMvc
                .perform(postRequest(requestOf(file, Instant.parse(updateDate))))
                .andExpect(status().isOk());
        mockMvc
                .perform(get("/node/file/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[*].date", containsInAnyOrder(createDate, updateDate)))
                .andExpect(jsonPath("$.items[*].size", containsInAnyOrder(77, 87)));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/fillUpdates.sql"})
    void allHistory() throws Exception {
        mockMvc
                .perform(get("/node/file/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(7)));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/fillUpdates.sql"})
    void dateEndExclusive() throws Exception {
        mockMvc
                .perform(get("/node/file/history?dateEnd=" + "2022-10-10T16:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(4)));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/fillUpdates.sql"})
    void dateEndInclusive() throws Exception {
        mockMvc
                .perform(get("/node/file/history?dateEnd=" + "2022-10-10T16:00:00.001Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(5)));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/fillUpdates.sql"})
    void dateStart() throws Exception {
        mockMvc
                .perform(get("/node/file/history?dateStart=" + "2022-10-10T17:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/fillUpdates.sql"})
    void dateStartAndDateEndExclusive() throws Exception {
        mockMvc
                .perform(get("/node/file/history?dateStart=" + "2022-10-10T14:00:00Z"
                        + "&dateEnd=" + "2022-10-10T16:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/fillUpdates.sql"})
    void dateStartAndDateEndInclusive() throws Exception {
        mockMvc
                .perform(get("/node/file/history?dateStart=" + "2022-10-10T14:00:00Z"
                        + "&dateEnd=" + "2022-10-10T16:00:00.001Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(3)));
    }
}
