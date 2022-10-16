package com.mayosen.academy;

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
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.time.Instant;
import java.util.List;

import static com.mayosen.academy.Utils.requestOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NodesTest {
    private final MockMvc mockMvc;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Autowired
    public NodesTest(MockMvc mockMvc, ResourceLoader resourceLoader) {
        this.mockMvc = mockMvc;
        this.resourceLoader = resourceLoader;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private MockHttpServletRequestBuilder postRequest(ItemImportRequest request) throws JsonProcessingException {
        byte[] bytes = objectMapper.writeValueAsBytes(request);
        return post("/imports").contentType(MediaType.APPLICATION_JSON).content(bytes);
    }

    private MockHttpServletRequestBuilder postRequest(String filename) throws Exception {
        String path = String.format("classpath:/imports/%s", filename);
        File file = resourceLoader.getResource(path).getFile();
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
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
        mockMvc.perform(postRequest("group.json")).andExpect(status().isOk());

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

    @Test
    @Sql({"/truncate.sql", "/fillWithGroup.sql"})
    void moveFileToRoot() throws Exception {
        ItemImport item = new ItemImport("f3", "f3-url", null, ItemType.FILE, 100L);
        String updateDate = "2022-10-10T12:00:00.000Z";
        mockMvc.perform(postRequest(requestOf(item, Instant.parse(updateDate)))).andExpect(status().isOk());

        mockMvc
                .perform(get("/nodes/c2"))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.size").value(110))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/a"))
                .andExpect(jsonPath("$.size").value(310))
                .andExpect(jsonPath("$.date").value(updateDate));
    }

    @Test
    @Sql({"/truncate.sql", "/fillWithGroup.sql"})
    void moveFileToOtherFolder() throws Exception {
        ItemImport item = new ItemImport("f3", "f3-url", "b3", ItemType.FILE, 100L);
        String updateDate = "2022-10-10T12:00:00.000Z";
        mockMvc.perform(postRequest(requestOf(item, Instant.parse(updateDate)))).andExpect(status().isOk());

        mockMvc
                .perform(get("/nodes/c2"))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.size").value(110))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/b3"))
                .andExpect(jsonPath("$.size").value(300))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/a"))
                .andExpect(jsonPath("$.size").value(410))
                .andExpect(jsonPath("$.date").value(updateDate));
    }

    @Test
    @Sql({"/truncate.sql", "/fillWithGroup.sql"})
    void moveFolderToOtherFolder() throws Exception {
        ItemImport item = new ItemImport("b1", null, "b3", ItemType.FOLDER, null);
        String updateDate = "2022-10-10T12:00:00.000Z";
        mockMvc.perform(postRequest(requestOf(item, Instant.parse(updateDate)))).andExpect(status().isOk());

        mockMvc
                .perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.size").value(210))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/b3"))
                .andExpect(jsonPath("$.size").value(410))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/a"))
                .andExpect(jsonPath("$.size").value(410))
                .andExpect(jsonPath("$.date").value(updateDate));
    }
}
