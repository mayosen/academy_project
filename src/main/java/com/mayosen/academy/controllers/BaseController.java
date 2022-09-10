package com.mayosen.academy.controllers;

import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import com.mayosen.academy.responses.items.ItemResponse;
import com.mayosen.academy.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;

@RestController
public class BaseController {
    private final ItemService itemService;

    @Autowired
    public BaseController(ItemService importService) {
        this.itemService = importService;
    }

    @PostMapping("/imports")
    public void updateItems(@Valid @RequestBody SystemItemImportRequest request) {
        itemService.insertOrUpdate(request);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteItem(@PathVariable String id, @RequestParam Instant date) {
        itemService.delete(id);
    }

    @GetMapping("/nodes/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable String id) {
        return ResponseEntity.ok(itemService.get(id));
    }
}
