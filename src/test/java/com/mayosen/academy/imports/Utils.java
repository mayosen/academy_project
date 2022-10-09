package com.mayosen.academy.imports;

import com.mayosen.academy.requests.ItemImport;
import com.mayosen.academy.requests.ItemImportRequest;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class Utils {
    public static ItemImportRequest requestOf(ItemImport item) {
        return new ItemImportRequest(List.of(item), Instant.now());
    }

    public static ItemImportRequest requestOf(List<ItemImport> items) {
        return new ItemImportRequest(items, Instant.now());
    }

    public static void expectValidationFailed(ResultActions actions) throws Exception {
        actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
