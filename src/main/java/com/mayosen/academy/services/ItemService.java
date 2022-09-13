package com.mayosen.academy.services;

import com.mayosen.academy.domain.ItemUpdate;
import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.SystemItemType;
import com.mayosen.academy.exceptions.ItemNotFoundException;
import com.mayosen.academy.repos.ItemUpdateRepo;
import com.mayosen.academy.repos.SystemItemRepo;
import com.mayosen.academy.requests.SystemItemImport;
import com.mayosen.academy.requests.SystemItemImportRequest;
import com.mayosen.academy.responses.items.ItemResponse;
import com.mayosen.academy.responses.updates.SystemItemHistoryResponse;
import com.mayosen.academy.responses.updates.SystemItemHistoryUnit;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Log4j2
@Service
public class ItemService {
    private final SystemItemRepo systemItemRepo;
    private final ItemUpdateRepo itemUpdateRepo;

    @Autowired
    public ItemService(SystemItemRepo systemItemRepo, ItemUpdateRepo itemUpdateRepo) {
        this.systemItemRepo = systemItemRepo;
        this.itemUpdateRepo = itemUpdateRepo;
    }

    @Transactional
    public void updateItems(SystemItemImportRequest request) {
        Instant updateDate = request.getUpdateDate();
        int itemsSize = request.getItems().size();
        Map<String, SystemItem> mappedItems = new HashMap<>(itemsSize);

        for (SystemItemImport importItem : request.getItems()) {
            Long size = importItem.getSize();

            SystemItem item = systemItemRepo.findById(importItem.getId()).orElseGet(SystemItem::new);

            if (item.getId() != null && importItem.getType() != item.getType()) {
                throw new ValidationException("Нельзя менять тип элемента");
            }

            if (importItem.getType() == SystemItemType.FOLDER) {
                if (importItem.getUrl() != null) {
                    throw new ValidationException("Поле url должно быть пустым у папки");
                } else if (importItem.getSize() != null) {
                    throw new ValidationException("Поле size должно быть пустым у папки");
                }
            } else {
                if (importItem.getUrl() == null) {
                    throw new ValidationException("Поле url не должно быть пустым у файла");
                } else if (importItem.getSize() == null || !(importItem.getSize() > 0)) {
                    throw new ValidationException("Поле size должно быть больше 0 у файла");
                }
            }

            item.setId(importItem.getId());
            item.setUrl(importItem.getUrl());
            item.setDate(updateDate);
            item.setParentId(importItem.getParentId());
            item.setType(importItem.getType());
            item.setSize(size);

            mappedItems.put(item.getId(), item);
        }

        for (SystemItem item : mappedItems.values()) {
            String parentId = item.getParentId();

            if (parentId != null) {
                SystemItem parent = mappedItems.get(parentId);

                if (parent == null) {
                    parent = systemItemRepo.findById(parentId)
                            .orElseThrow(() -> new ValidationException("Родитель не найден"));
                }

                if (parent.getType() != SystemItemType.FOLDER) {
                    throw new ValidationException("Родителем может быть только папка");
                }

                item.setParent(parent);

                if (!item.isPersisted()) {
                    // Если Entity еще не сохранен, приходится заполнять детей руками
                    // Дети используются при подсчете размера папки
                    parent.getChildren().add(item);
                }
            }
        }

        Map<String, Long> knownSizes = new HashMap<>(itemsSize);
        Set<SystemItem> sortedItems = new LinkedHashSet<>(itemsSize);
        List<ItemUpdate> updates = new ArrayList<>(itemsSize);

        // Сортировка нужна, чтобы сохранить родителей вперед детей
        for (SystemItem item : mappedItems.values()) {
            SystemItem current = item;
            current.setSize(getItemSize(current, knownSizes));
            Deque<SystemItem> childBranch = new LinkedList<>();
            childBranch.addLast(current);

            while (current.getParent() != null) {
                current = current.getParent();
                current.setSize(getItemSize(current, knownSizes));
                childBranch.addFirst(current);
            }

            sortedItems.addAll(childBranch);
        }

        for (SystemItem item : sortedItems) {
            item.getChildren().clear();  // Для корректного сохранения
            updates.add(new ItemUpdate(item));
        }

        systemItemRepo.saveAll(sortedItems);
        itemUpdateRepo.saveAll(updates);
    }

    private void updateParents(SystemItem item, Instant updateDate, Map<String, Long> knownSizes) {
        SystemItem current = item;
        ItemUpdate update;

        while (current.getParent() != null) {
            current = current.getParent();
            current.setDate(updateDate);
            current.setSize(getItemSize(current, knownSizes));
            update = new ItemUpdate(current);
            systemItemRepo.save(current);
            itemUpdateRepo.save(update);
        }
    }

    private SystemItem findById(String id) {
        return systemItemRepo.findById(id).orElseThrow(ItemNotFoundException::new);
    }

    @Transactional
    public void deleteItem(String id, Instant updateDate) {
        SystemItem item = findById(id);
        systemItemRepo.delete(item);
        updateParents(item, updateDate, new HashMap<>());
    }

    @Transactional
    public ItemResponse getNode(String id) {
        SystemItem rootItem = findById(id);
        ItemResponse response = new ItemResponse(rootItem);
        setChildren(response, rootItem.getChildren());
        return response;
    }

    private Long setChildren(ItemResponse response, List<SystemItem> itemChildren) {
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

            response.setSize(size);
        }

        response.setChildren(responseChildren);
        return size;
    }

    @Transactional
    public SystemItemHistoryResponse getLastUpdates(Instant dateTo) {
        // TODO: Переделать

        Instant dateFrom = dateTo.minus(24, ChronoUnit.HOURS);
        List<SystemItem> items = systemItemRepo.findAllByDateBetween(dateFrom, dateTo);

        List<SystemItemHistoryUnit> units = new ArrayList<>(items.size());
        Map<String, Long> folderSizes = new HashMap<>();
        SystemItem parent;
        Long size;

        for (SystemItem item : items) {
            SystemItemHistoryUnit unit = new SystemItemHistoryUnit();
            unit.setId(item.getId());
            unit.setUrl(item.getUrl());
            parent = item.getParent();
            unit.setParentId(parent == null ? null : parent.getId());
            unit.setType(item.getType());
            size = getItemSize(item, folderSizes);
            unit.setSize(size);
            unit.setDate(item.getDate());
            units.add(unit);
        }

        return new SystemItemHistoryResponse(units);
    }

    private Long getItemSize(SystemItem item, Map<String, Long> knownSizes) {
        Long size;

        if (item.getType() == SystemItemType.FILE) {
            size = item.getSize();
        } else {
            size = knownSizes.get(item.getId());

            if (size == null) {
                size = 0L;
                List<SystemItem> children = item.getChildren();

                if (children != null) {
                    for (SystemItem child : children) {
                        Long currentSize = getItemSize(child, knownSizes);
                        size += currentSize;
                    }
                }

                knownSizes.put(item.getId(), size);
            }
        }

        return size;
    }

    @Transactional
    public SystemItemHistoryResponse getNodeHistory(String id, Instant dateStart, Instant dateEnd) {
        SystemItem item = findById(id);
        List<ItemUpdate> updates;

        if (dateStart == null && dateEnd == null) {
            updates = itemUpdateRepo.findAllByItem(item);
        } else if (dateStart != null && dateEnd != null) {
            updates = itemUpdateRepo.findAllByItemAndDateInterval(item, dateStart, dateEnd);
        } else if (dateStart != null) {
            updates = itemUpdateRepo.findAllByItemAndDateFrom(item, dateStart);
        } else {
            updates = itemUpdateRepo.findAllByItemAndDateTo(item, dateEnd);
        }

        List<SystemItemHistoryUnit> units = updates.stream().map(SystemItemHistoryUnit::new).toList();

        return new SystemItemHistoryResponse(units);

    }
}
