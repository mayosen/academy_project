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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static com.mayosen.academy.imports.Utils.expectValidationFailed;
import static com.mayosen.academy.imports.Utils.requestOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class ImportsTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImportsTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private MockHttpServletRequestBuilder postRequest(ItemImportRequest request) throws JsonProcessingException {
        byte[] bytes = objectMapper.writeValueAsBytes(request);
        return post("/imports").contentType(MediaType.APPLICATION_JSON).content(bytes);
    }

    @Test
    public void emptyRequest() throws Exception {
        expectValidationFailed(mockMvc.perform(post("/imports")));
    }

    @Test
    public void nullUpdateDate() throws Exception {
        ItemImportRequest request = new ItemImportRequest(Collections.emptyList(), null);
        byte[] bytes = objectMapper.writeValueAsBytes(request);
        expectValidationFailed(mockMvc
                .perform(post("/imports").contentType(MediaType.APPLICATION_JSON).content(bytes)));
    }

    @Test
    public void nullItemsList() throws Exception {
        ItemImportRequest request = new ItemImportRequest(null, Instant.now());
        expectValidationFailed(mockMvc.perform(postRequest(request)));
    }

    @Test
    public void nullItemId() throws Exception {
        ItemImport item = new ItemImport();
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    public void blankItemId() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FILE, 40L);
        mockMvc
                .perform(postRequest(requestOf(item)))
                .andExpect(status().isOk());
    }

    @Test
    public void notUniqueItems() throws Exception {
        List<ItemImport> items = List.of(
                new ItemImport("", "", null, ItemType.FILE, 40L),
                new ItemImport("", "", null, ItemType.FILE, 40L)
        );
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(items))));
    }

    @Test
    public void notNullFolderUrl() throws Exception {
        ItemImport item = new ItemImport("", "url", null, ItemType.FOLDER, null);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    public void nullFileUrl() throws Exception {
        ItemImport item = new ItemImport("", null, null, ItemType.FILE, 40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    public void notNullFolderSize() throws Exception {
        ItemImport item = new ItemImport("", null, null, ItemType.FOLDER, 40L);
        mockMvc
                .perform(postRequest(requestOf(item)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    public void nullFileSize() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FOLDER, null);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    public void zeroFileSize() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FILE, 0L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    public void negativeFileSize() throws Exception {
        ItemImport item = new ItemImport("", "", null, ItemType.FILE, -40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }

    @Test
    public void tooLongFileUrl() throws Exception {
        String url = "longUrl".repeat(100);
        ItemImport item = new ItemImport("", url, null, ItemType.FILE, 40L);
        expectValidationFailed(mockMvc.perform(postRequest(requestOf(item))));
    }
}
