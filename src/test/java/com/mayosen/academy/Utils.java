package com.mayosen.academy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mayosen.academy.requests.ItemImport;
import com.mayosen.academy.requests.ItemImportRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class Utils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public static ItemImportRequest requestOf(ItemImport item, Instant updateDate) {
        return new ItemImportRequest(List.of(item), updateDate);
    }

    public static ItemImportRequest requestOf(ItemImport item) {
        return requestOf(item, Instant.now());
    }

    public static ItemImportRequest requestOf(List<ItemImport> items, Instant updateDate) {
        return new ItemImportRequest(items, updateDate);
    }

    public static ItemImportRequest requestOf(List<ItemImport> items) {
        return requestOf(items, Instant.now());
    }

    public static MockHttpServletRequestBuilder postRequest(ItemImportRequest request) throws JsonProcessingException {
        byte[] bytes = objectMapper.writeValueAsBytes(request);
        return post("/imports").contentType(MediaType.APPLICATION_JSON).content(bytes);
    }
}
