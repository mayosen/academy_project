package com.mayosen.academy.controllers;

import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import com.mayosen.academy.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;

@RestController
public class BaseController {
    private final ItemService importService;

    @Autowired
    public BaseController(ItemService importService) {
        this.importService = importService;
    }

    @PostMapping("/imports")
    public void updateItems(@Valid @RequestBody SystemItemImportRequest request) {
        importService.insertOrUpdate(request);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteItem(@PathVariable String id, @RequestParam Instant date) {
        importService.delete(id);
    }
}
