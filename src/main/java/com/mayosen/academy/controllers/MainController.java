package com.mayosen.academy.controllers;

import com.mayosen.academy.requests.ItemImportRequest;
import com.mayosen.academy.responses.items.ItemResponse;
import com.mayosen.academy.responses.updates.ItemHistoryResponse;
import com.mayosen.academy.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;

@RestController
public class MainController {
    private final ItemService itemService;

    @Autowired
    public MainController(ItemService importService) {
        this.itemService = importService;
    }

    @PostMapping("/imports")
    public void updateItems(@Valid @RequestBody ItemImportRequest request) {
        itemService.updateItems(request);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteItem(@PathVariable String id, @RequestParam Instant date) {
        itemService.deleteItem(id, date);
    }

    @DeleteMapping("/delete/")
    public void deleteItemWithBlankId(@RequestParam Instant date) {
        itemService.deleteItem("", date);
    }

    @GetMapping("/nodes/{id}")
    public ResponseEntity<ItemResponse> getNode(@PathVariable String id) {
        return ResponseEntity.ok(itemService.getNode(id));
    }

    @GetMapping("/nodes/")
    public ResponseEntity<ItemResponse> getNodeWithBlankId() {
        return ResponseEntity.ok(itemService.getNode(""));
    }

    @GetMapping("/updates")
    public ResponseEntity<ItemHistoryResponse> getLastUpdatedFiles(@RequestParam Instant date) {
        return ResponseEntity.ok(itemService.getLastUpdatedFiles(date));
    }

    @GetMapping("/node/{id}/history")
    public ResponseEntity<ItemHistoryResponse> getNodeHistory(
            @PathVariable String id,
            @RequestParam(required = false) Instant dateStart,
            @RequestParam(required = false) Instant dateEnd
    ) {
        return ResponseEntity.ok(itemService.getNodeHistory(id, dateStart, dateEnd));
    }

    @GetMapping("/node//history")
    public ResponseEntity<ItemHistoryResponse> getNodeWithBlankIdHistory(
            @RequestParam(required = false) Instant dateStart,
            @RequestParam(required = false) Instant dateEnd
    ) {
        return ResponseEntity.ok(itemService.getNodeHistory("", dateStart, dateEnd));
    }
}
