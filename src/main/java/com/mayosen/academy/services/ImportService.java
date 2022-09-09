package com.mayosen.academy.services;

import com.mayosen.academy.SystemItemRepo;
import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.requests.imports.SystemItemImport;
import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
public class ImportService {
    private final SystemItemRepo systemItemRepo;

    @Autowired
    public ImportService(SystemItemRepo systemItemRepo) {
        this.systemItemRepo = systemItemRepo;
    }

    @Transactional
    public void insertOrUpdate(SystemItemImportRequest request) {
        List<SystemItem> items = request.getItems()
                .stream()
                .map(r -> {
                    SystemItem item = new SystemItem();
                    item.setId(r.getId());
                    item.setUrl(r.getUrl());
                    item.setDate(request.getUpdateDate());
                    item.setParentId(r.getParentId());
                    item.setType(r.getType());
                    item.setSize(r.getSize());
                    // TODO: Тут должна быть более сложная логика
                    return item;
                })
                .toList();

        systemItemRepo.saveAll(items);
    }
}
