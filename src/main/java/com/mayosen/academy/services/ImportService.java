package com.mayosen.academy.services;

import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.SystemItemType;
import com.mayosen.academy.repos.SystemItemRepo;
import com.mayosen.academy.requests.imports.SystemItemImport;
import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class ImportService {
    private final SystemItemRepo systemItemRepo;

    @Autowired
    public ImportService(SystemItemRepo systemItemRepo) {
        this.systemItemRepo = systemItemRepo;
    }

    @Transactional
    public void insertOrUpdate(SystemItemImportRequest request) {
        List<SystemItemImport> folders = new ArrayList<>();
        List<SystemItemImport> rootFolders = new ArrayList<>();
        List<SystemItemImport> files = new ArrayList<>();

        for (SystemItemImport current : request.getItems()) {
            if (current.getType() == SystemItemType.FOLDER) {
                if (current.getParentId() == null) {
                    rootFolders.add(current);
                } else {
                    folders.add(current);
                }
            } else {
                files.add(current);
            }
        }

        log.debug(String.format(
                "Separated request: root folders: %d, folders: %d, files: %d",
                rootFolders.size(), folders.size(), files.size())
        );

        Map<String, SystemItem> items = new HashMap<>();
        saveAll(rootFolders, items, request.getUpdateDate());
        saveAll(folders, items, request.getUpdateDate());
        saveAll(files, items, request.getUpdateDate());
    }

    private void saveAll(List<SystemItemImport> imports, Map<String, SystemItem> items, Instant updateDate) {
        List<SystemItem> toSave = new ArrayList<>();

        for (SystemItemImport current : imports) {
            SystemItem item = systemItemRepo.findById(current.getId()).orElseGet(SystemItem::new);

            if (item.getId() != null && current.getType() != item.getType()) {
                throw new ValidationException("Нельзя менять тип элемента");
            }

            if (current.getType() == SystemItemType.FOLDER) {
                if (current.getUrl() != null) {
                    throw new ValidationException("Поле url должно быть пустым у папки");
                } else if (current.getSize() != null) {
                    throw new ValidationException("Поле size должно быть пустым у папки");
                }
            }

            SystemItem parent = null;

            if (current.getParentId() != null) {
                parent = items.get(current.getParentId());

                if (parent == null) {
                    parent = systemItemRepo.findById(current.getParentId())
                            .orElseThrow(() -> new ValidationException("Родитель с таким id не существует"));
                }

                if (parent.getType() != SystemItemType.FOLDER) {
                    throw new ValidationException("Родителем может быть только папка");
                }
            }

            item.setId(current.getId());
            item.setUrl(current.getUrl());
            item.setDate(updateDate);
            item.setParentId(current.getParentId());
            item.setType(current.getType());
            item.setSize(current.getSize());
            items.put(item.getId(), item);
            toSave.add(item);
        }

        systemItemRepo.saveAll(toSave);
    }
}
