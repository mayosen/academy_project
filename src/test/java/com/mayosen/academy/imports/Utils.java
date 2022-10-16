package com.mayosen.academy.imports;

import com.mayosen.academy.requests.ItemImport;
import com.mayosen.academy.requests.ItemImportRequest;

import java.time.Instant;
import java.util.List;

public class Utils {
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
}
