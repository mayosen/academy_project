package com.mayosen.academy.controllers;

import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import com.mayosen.academy.services.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class BaseController {
    private final ImportService importService;

    @Autowired
    public BaseController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/imports")
    public void updateItems(@Valid @RequestBody SystemItemImportRequest request) {
        importService.insertOrUpdate(request);
    }
}
