package com.mayosen.academy.services;

import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.SystemItemType;
import com.mayosen.academy.exceptions.ItemNotFoundException;
import com.mayosen.academy.repos.SystemItemRepo;
import com.mayosen.academy.requests.imports.SystemItemImport;
import com.mayosen.academy.requests.imports.SystemItemImportRequest;
import com.mayosen.academy.responses.items.ItemResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.time.Instant;
import java.util.*;

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
        List<SystemItemImport> folders = new ArrayList<>();
        List<SystemItemImport> rootFolders = new ArrayList<>();
        List<SystemItemImport> files = new ArrayList<>();
        Map<String, SystemItemImport> items = new HashMap<>();

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
            items.put(current.getId(), current);
        }

        log.debug(String.format(
                "Separated request: root folders: %d, folders: %d, files: %d",
                rootFolders.size(), folders.size(), files.size())
        );

        saveAll(rootFolders, items, request.getUpdateDate());
        saveAll(folders, items, request.getUpdateDate());
        saveAll(files, items, request.getUpdateDate());
    }

    private void saveAll(List<SystemItemImport> imports, Map<String, SystemItemImport> items, Instant updateDate) {
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

            if (current.getParentId() != null) {
                SystemItemType parentType;
                SystemItemImport parentImport = items.get(current.getParentId());

                if (parentImport == null) {
                    parentType = findById(current.getParentId()).getType();
                } else {
                    parentType = parentImport.getType();
                }

                if (parentType != SystemItemType.FOLDER) {
                    throw new ValidationException("Родителем может быть только папка");
                }
            }

            item.setId(current.getId());
            item.setUrl(current.getUrl());
            item.setDate(updateDate);
            item.setParentId(current.getParentId());
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
    public void delete(String id) {
        SystemItem item = findById(id);
        systemItemRepo.delete(item);
    }

    public ItemResponse get(String id) {
        SystemItem rootItem = findById(id);
        ItemResponse rootResponse = mapItemToItemResponse(rootItem);
        Queue<Pair> childrenQueue = new LinkedList<>();
        childrenQueue.add(new Pair(rootResponse.getChildren(), rootItem));

        while (childrenQueue.peek() != null) {
            Pair pair = childrenQueue.poll();
            List<SystemItem> children = systemItemRepo.findChildrenByItemId(pair.getCurrent().getId());

            for (SystemItem child : children) {
                ItemResponse response = mapItemToItemResponse(child);
                pair.getParentList().add(response);
                childrenQueue.add(new Pair(response.getChildren(), child));
            }
        }

        return rootResponse;
    }

    private ItemResponse mapItemToItemResponse(SystemItem item) {
        return new ItemResponse(
                item.getId(),
                item.getUrl(),
                item.getType(),
                item.getParentId(),
                item.getDate(),
                item.getSize(),
                new ArrayList<>()
        );
    }

    @Getter
    @AllArgsConstructor
    private class Pair {
        private List<ItemResponse> parentList;
        private SystemItem current;
    }
}
