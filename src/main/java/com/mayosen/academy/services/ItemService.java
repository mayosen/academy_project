package com.mayosen.academy.services;

import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.SystemItemType;
import com.mayosen.academy.exceptions.ItemNotFoundException;
import com.mayosen.academy.repos.SystemItemRepo;
import com.mayosen.academy.requests.imports.SystemItemImport;
import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import com.mayosen.academy.responses.items.ItemResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class ItemService {
    private final SystemItemRepo systemItemRepo;

    @Autowired
    public ItemService(SystemItemRepo systemItemRepo) {
        this.systemItemRepo = systemItemRepo;
    }

    @Transactional
    public void insertOrUpdate(SystemItemImportRequest request) {
        List<SystemItemImport> rootItems = new ArrayList<>();
        List<SystemItemImport> folders = new ArrayList<>();
        List<SystemItemImport> files = new ArrayList<>();

        for (SystemItemImport current : request.getItems()) {
            if (current.getType() == SystemItemType.FOLDER) {
                if (current.getParentId() == null) {
                    rootItems.add(current);
                } else {
                    folders.add(current);
                }
            } else {
                files.add(current);
            }
        }

        log.debug(String.format(
                "Separating request: root items: %d, folders: %d, files: %d",
                rootItems.size(), folders.size(), files.size())
        );

        Instant updateDate = request.getUpdateDate();
        saveAll(rootItems, updateDate);
        saveAll(folders, updateDate);
        saveAll(files, updateDate);
    }

    private void saveAll(List<SystemItemImport> imports, Instant updateDate) {
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
            } else if (current.getType() == SystemItemType.FILE) {
                if (current.getSize() == null || !(current.getSize() > 0)) {
                    throw new ValidationException("Поле size должно быть больше 0 у файла");
                }
            }

            SystemItem parent = null;

            if (current.getParentId() != null) {
                // Поскольку папки сохраняются первее файлов, родитель уже существует в базе
                parent = systemItemRepo.findById(current.getParentId())
                        .orElseThrow(() -> new ValidationException("Родитель не найден"));

                if (parent.getType() != SystemItemType.FOLDER) {
                    throw new ValidationException("Родителем может быть только папка");
                }
            }

            item.setId(current.getId());
            item.setUrl(current.getUrl());
            item.setDate(updateDate);
            item.setParent(parent);
            item.setType(current.getType());
            item.setSize(current.getSize());
            toSave.add(item);
        }

        systemItemRepo.saveAll(toSave);
    }

    private SystemItem findById(String id) {
        return systemItemRepo.findById(id).orElseThrow(ItemNotFoundException::new);
    }

    @Transactional
    public void delete(String id, Instant updateDate) {
        SystemItem item = findById(id);
        SystemItem current = item;
        List<SystemItem> parents = new ArrayList<>();

        while (current.getParent() != null) {
            current = current.getParent();
            current.setDate(updateDate);
            parents.add(current);
        }

        systemItemRepo.delete(item);
        systemItemRepo.saveAll(parents);
    }

    public ItemResponse getNode(String id) {
        SystemItem rootItem = findById(id);
        ItemResponse response = new ItemResponse(rootItem);
        setChildren(response, rootItem.getChildren());
        return response;
    }

    public Long setChildren(ItemResponse response, List<SystemItem> itemChildren) {
        Long size = 0L;
        List<ItemResponse> responseChildren = null;

        if (response.getType() == SystemItemType.FOLDER) {
            responseChildren = new ArrayList<>();
            ItemResponse currentResponse;
            Long currentSize;

            if (itemChildren != null) {
                for (SystemItem item : itemChildren) {
                    currentResponse = new ItemResponse(item);

                    if (item.getType() == SystemItemType.FILE) {
                        currentSize = item.getSize();
                        currentResponse.setChildren(null);
                    } else {
                        currentSize = setChildren(currentResponse, item.getChildren());
                    }

                    currentResponse.setSize(currentSize);
                    responseChildren.add(currentResponse);
                    size += currentSize;
                }
            }
        }

        response.setChildren(responseChildren);
        response.setSize(size);
        return size;
    }
}
