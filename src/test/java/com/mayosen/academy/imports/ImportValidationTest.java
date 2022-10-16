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
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static com.mayosen.academy.imports.Utils.requestOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImportValidationTest {
    private final MockMvc mockMvc;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImportValidationTest(MockMvc mockMvc, ResourceLoader resourceLoader) {
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

    private void expectValidationFailed(ResultActions actions) throws Exception {
        actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void emptyRequest() throws Exception {
        expectValidationFailed(mockMvc.perform(post("/imports")));
    }

    @Test
    void nullUpdateDate() throws Exception {
        ItemImportRequest request = new ItemImportRequest(Collections.emptyList(), null);
        expectValidationFailed(mockMvc.perform(postRequest(request)));
    }

    @Test
    void nullItemsList() throws Exception {
        ItemImportRequest request = new ItemImportRequest(null, Instant.now());
        expectValidationFailed(mockMvc.perform(postRequest(request)));
    }

    @Test
    void nullItemId() throws Exception {
        ItemImport item = new ItemImport();
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void blankItemId() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FILE, 40L);
        mockMvc
                .perform(postRequest(requestOf(item)))
                .andExpect(status().isOk());
    }

    @Test
    void notUniqueItems() throws Exception {
        List<ItemImport> items = List.of(
                new ItemImport("", "", null, ItemType.FILE, 40L),
                new ItemImport("", "", null, ItemType.FILE, 40L)
        );
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(items))));
    }

    @Test
    void notNullFolderUrl() throws Exception {
        ItemImport item = new ItemImport("", "url", null, ItemType.FOLDER, null);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void nullFileUrl() throws Exception {
        ItemImport item = new ItemImport("", null, null, ItemType.FILE, 40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void notNullFolderSize() throws Exception {
        ItemImport item = new ItemImport("", null, null, ItemType.FOLDER, 40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void nullFileSize() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FOLDER, null);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void zeroFileSize() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FILE, 0L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void negativeFileSize() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FILE, -40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void tooLongFileUrl() throws Exception {
        String url = "longUrl".repeat(100);
        ItemImport item = new ItemImport("", url, null, ItemType.FILE, 40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    void nullParent() throws Exception {
        ItemImport item = new ItemImport("id", null, null, ItemType.FOLDER, null);
        mockMvc
                .perform(postRequest(requestOf(item)))
                .andExpect(status().isOk());
    }

    @Test
    @Sql("/truncate.sql")
    void fileAsParent() throws Exception {
        expectValidationFailed(mockMvc.perform(postRequest("fileAsParent.json")));
    }

    @Test
    @Sql("/truncate.sql")
    void folderAsParent() throws Exception {
        mockMvc
                .perform(postRequest("folderAsParent.json"))
                .andExpect(status().isOk());
    }

    @Test
    @Sql("/truncate.sql")
    void notExistingParent() throws Exception {
        ItemImport item = new ItemImport("item", "", "notExistingParent", ItemType.FILE, 40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    @Sql("/truncate.sql")
    void existingParent() throws Exception {
        ItemImport folder = new ItemImport("folder", null, null, ItemType.FOLDER, null);
        mockMvc
                .perform(postRequest(requestOf(folder)))
                .andExpect(status().isOk());

        ItemImport file = new ItemImport("file", "", "folder", ItemType.FILE, 40L);
        mockMvc
                .perform(postRequest(requestOf(file)))
                .andExpect(status().isOk());
    }
}
