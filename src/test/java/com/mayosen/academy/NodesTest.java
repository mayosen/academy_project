package com.mayosen.academy;

import com.mayosen.academy.domain.ItemType;
import com.mayosen.academy.requests.ItemImport;
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

import static com.mayosen.academy.Utils.postRequest;
import static com.mayosen.academy.Utils.requestOf;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private MockHttpServletRequestBuilder postFileRequest(String filename) throws Exception {
        String path = String.format("classpath:data%s", filename);
        File file = resourceLoader.getResource(path).getFile();
        byte[] bytes = FileCopyUtils.copyToByteArray(file);
        return post("/imports").contentType(MediaType.APPLICATION_JSON).content(bytes);
    }

    @Test
    @Sql("/sql/truncate.sql")
    void updateDateWithoutContentChanges() throws Exception {
        List<ItemImport> items = List.of(
                new ItemImport("parent", null, null, ItemType.FOLDER, null),
                new ItemImport("child", null, "parent", ItemType.FOLDER, null)
        );

        String createDate = "2022-10-10T00:00:00Z";
        mockMvc
                .perform(postRequest(requestOf(items, Instant.parse(createDate))))
                .andExpect(status().isOk());
        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value(createDate));
        mockMvc
                .perform(get("/nodes/child"))
                .andExpect(jsonPath("$.date").value(createDate));

        String updateDate = "2022-10-15T00:00:00Z";
        mockMvc
                .perform(postRequest(requestOf(items, Instant.parse(updateDate))))
                .andExpect(status().isOk());
        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value(updateDate));
        mockMvc
                .perform(get("/nodes/child"))
                .andExpect(jsonPath("$.date").value(updateDate));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void updateParentDate() throws Exception {
        ItemImport parent = new ItemImport("parent", null, null, ItemType.FOLDER, null);
        ItemImport child = new ItemImport("child", null, "parent", ItemType.FOLDER, null);

        mockMvc
                .perform(postRequest(requestOf(List.of(parent, child), Instant.parse("2022-10-10T00:00:00Z"))))
                .andExpect(status().isOk());

        mockMvc
                .perform(postRequest(requestOf(child, Instant.parse("2022-10-15T00:00:00.2Z"))))
                .andExpect(status().isOk());

        String expectedDate = "2022-10-15T00:00:00.200Z";
        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/parent"))
                .andExpect(jsonPath("$.date").value(expectedDate));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void notFound() throws Exception {
        mockMvc
                .perform(get("/nodes/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Item not found"));
    }

    @Test
    @Sql("/sql/truncate.sql")
    void getItemWithBlankId() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FILE, 100L);
        mockMvc.perform(postRequest(requestOf(item))).andExpect(status().isOk());

        mockMvc
                .perform(get("/nodes/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(""))
                .andExpect(jsonPath("$.url").value(""))
                .andExpect(jsonPath("$.type").value("FILE"))
                .andExpect(jsonPath("$.parentId").doesNotExist())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.children").doesNotExist());
    }

    @Test
    @Sql("/sql/truncate.sql")
    void sizes() throws Exception {
        mockMvc.perform(postFileRequest("/group.json")).andExpect(status().isOk());

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
    @Sql({"/sql/truncate.sql", "/sql/importGroup.sql"})
    void moveFileToRoot() throws Exception {
        ItemImport item = new ItemImport("f3", "f3-url", null, ItemType.FILE, 100L);
        String updateDate = "2022-10-10T12:00:00.000Z";
        mockMvc.perform(postRequest(requestOf(item, Instant.parse(updateDate)))).andExpect(status().isOk());

        String expectedDate = "2022-10-10T12:00:00Z";
        mockMvc
                .perform(get("/nodes/c2"))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.size").value(110))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/a"))
                .andExpect(jsonPath("$.size").value(310))
                .andExpect(jsonPath("$.date").value(expectedDate));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/importGroup.sql"})
    void moveFileToOtherFolder() throws Exception {
        ItemImport item = new ItemImport("f3", "f3-url", "b3", ItemType.FILE, 100L);
        String updateDate = "2022-10-10T12:00:00.000Z";
        mockMvc.perform(postRequest(requestOf(item, Instant.parse(updateDate)))).andExpect(status().isOk());

        String expectedDate = "2022-10-10T12:00:00Z";
        mockMvc
                .perform(get("/nodes/c2"))
                .andExpect(jsonPath("$.size").value(0))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.size").value(110))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/b3"))
                .andExpect(jsonPath("$.size").value(300))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/a"))
                .andExpect(jsonPath("$.size").value(410))
                .andExpect(jsonPath("$.date").value(expectedDate));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/importGroup.sql"})
    void moveFolderToOtherFolder() throws Exception {
        ItemImport item = new ItemImport("b1", null, "b3", ItemType.FOLDER, null);
        String updateDate = "2022-10-10T12:00:00.000Z";
        mockMvc.perform(postRequest(requestOf(item, Instant.parse(updateDate)))).andExpect(status().isOk());

        String expectedDate = "2022-10-10T12:00:00Z";
        mockMvc
                .perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.size").value(210))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/b3"))
                .andExpect(jsonPath("$.size").value(410))
                .andExpect(jsonPath("$.date").value(expectedDate));
        mockMvc
                .perform(get("/nodes/a"))
                .andExpect(jsonPath("$.size").value(410))
                .andExpect(jsonPath("$.date").value(expectedDate));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/importGroup.sql"})
    void childrenTest() throws Exception {
        mockMvc
                .perform(get("/nodes/c1"))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].id", is("f6")));
        mockMvc
                .perform(get("/nodes/c2"))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].id", is("f3")));
        mockMvc
                .perform(get("/nodes/b2"))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(0)));
        mockMvc
                .perform(get("/nodes/b3"))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.children[0].id", is("c1")));
        mockMvc
                .perform(get("/nodes/b1"))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(3)))
                .andExpect(jsonPath("$.children[*].id", containsInAnyOrder("c2", "f1", "f2")));
        mockMvc
                .perform(get("/nodes/a"))
                .andExpect(jsonPath("$.children").isArray())
                .andExpect(jsonPath("$.children", hasSize(3)))
                .andExpect(jsonPath("$.children[*].id", containsInAnyOrder("b1", "b2", "b3")));
    }

    @Test
    @Sql({"/sql/truncate.sql", "/sql/importGroup.sql"})
    void filesHasNullChildren() throws Exception {
        mockMvc.perform(get("/nodes/f1")).andExpect(jsonPath("$.children").doesNotExist());
        mockMvc.perform(get("/nodes/f2")).andExpect(jsonPath("$.children").doesNotExist());
        mockMvc.perform(get("/nodes/f3")).andExpect(jsonPath("$.children").doesNotExist());
        mockMvc.perform(get("/nodes/f4")).andExpect(jsonPath("$.children").doesNotExist());
        mockMvc.perform(get("/nodes/f6")).andExpect(jsonPath("$.children").doesNotExist());
    }
}
