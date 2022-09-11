package com.mayosen.academy.controllers;

import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import com.mayosen.academy.responses.items.ItemResponse;
import com.mayosen.academy.responses.updates.SystemItemHistoryResponse;
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

    @DeleteMapping({"/delete/{id}", "/delete"})
    public void deleteItem(@PathVariable(required = false) String id, @RequestParam Instant date) {
        itemService.delete(id, date);
    }

    @GetMapping({"/nodes/{id}", "/nodes"})
    public ResponseEntity<ItemResponse> getItem(@PathVariable(required = false) String id) {
        return ResponseEntity.ok(itemService.getNode(id));
    }

    @GetMapping("/updates")
    public ResponseEntity<SystemItemHistoryResponse> getUpdates(@RequestParam Instant date) {
        return ResponseEntity.ok(itemService.getUpdates(date));
    }
}
