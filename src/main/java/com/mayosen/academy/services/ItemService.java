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

    /**
     * Добавление и обновление элементов.
     * @param request объект с обновляемыми элементами
     * @throws ValidationException ошибка валидации данных
     */
    @Transactional
    public void updateItems(SystemItemImportRequest request) {
        Instant updateDate = request.getUpdateDate();
        int itemsSize = request.getItems().size();
        // Таблица для быстрого поиска новых родителей из запроса
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
            item.setNewParentId(importItem.getParentId());
            item.setType(importItem.getType());
            item.setSize(size);

            mappedItems.put(item.getId(), item);
        }

        List<ItemParentPair> oldParents = new ArrayList<>();

        for (SystemItem item : mappedItems.values()) {
            String newParentId = item.getNewParentId();

            // Если есть старый родитель, id которого отличается от обновляемого (включая null)
            if (item.getParent() != null && !item.getParent().getId().equals(newParentId)) {
                oldParents.add(new ItemParentPair(item));
            }

            if (newParentId != null) {
                SystemItem parent = mappedItems.get(newParentId);

                if (parent == null) {
                    parent = systemItemRepo.findById(newParentId)
                            .orElseThrow(() -> new ValidationException("Родитель не найден"));
                }

                if (parent.getType() != SystemItemType.FOLDER) {
                    throw new ValidationException("Родителем может быть только папка");
                }

                item.setParent(parent);
                parent.getChildren().add(item);
            }
        }

        Map<String, Long> knownSizes = new HashMap<>(itemsSize);
        Set<SystemItem> sortedItems = new LinkedHashSet<>(itemsSize);
        List<ItemUpdate> updates = new ArrayList<>(itemsSize);

        for (ItemParentPair pair : oldParents) {
            SystemItem oldParent = pair.oldParent;
            SystemItem item = pair.item;
            oldParent.getChildren().remove(item);
            SystemItem current = item;
            Set<SystemItem> newParentsBranch = new HashSet<>();

            while (current.getParent() != null) {
                current = current.getParent();
                newParentsBranch.add(current);
            }

            updateParents(oldParent, item.getSize(), updateDate, newParentsBranch);
        }

        // Сортировка нужна, чтобы сохранить родителей вперед детей
        for (SystemItem item : mappedItems.values()) {
            Long size;
            SystemItem current = item;
            Deque<SystemItem> childBranch = new LinkedList<>();

            // Устанавливаем размер новым родителям
            do {
                size = getItemSize(current, knownSizes);
                current.setDate(updateDate);
                current.setSize(size);
                childBranch.addFirst(current);
                current = current.getParent();
            } while (current != null);

            sortedItems.addAll(childBranch);
        }

        for (SystemItem item : sortedItems) {
            item.getChildren().clear();  // Для корректного сохранения
            updates.add(new ItemUpdate(item));
        }

        systemItemRepo.saveAll(sortedItems);
        itemUpdateRepo.saveAll(updates);
    }

    /**
     * Класс для связи элемента и его старого родителя.
     */
    private static class ItemParentPair {
        SystemItem oldParent;
        SystemItem item;

        public ItemParentPair(SystemItem item) {
            this.oldParent = item.getParent();
            this.item = item;
        }
    }

    /**
     * Удаление элемента.
     * @param id идентификатор элемента
     * @param updateDate дата обновления
     */
    @Transactional
    public void deleteItem(String id, Instant updateDate) {
        SystemItem item = findById(id);
        systemItemRepo.delete(item);

        if (item.getParent() != null) {
            updateParents(item.getParent(), item.getSize(), updateDate, Collections.emptySet());
        }
    }

    /**
     * Поиск элемента.
     * @param id идентификатор элемента
     * @return найденный элемент
     * @throws ItemNotFoundException запрашиваемый элемент не найден
     */
    private SystemItem findById(String id) {
        return systemItemRepo.findById(id).orElseThrow(ItemNotFoundException::new);
    }

    /**
     * Обновление родителей при удалении одного элемента.
     * Их размер уменьшается на размер этого элемента, и устанавливается дата обновления.
     * @param rootParent первый родитель удаляемого элемента
     * @param itemSize размер удаляемого элемента
     * @param updateDate дата обновления
     * @param newParentsBranch множество новых родителей элемента.
     *                         Используется, чтобы не уменьшать их размер,
     *                         который будет обновлен во внешнем методе
     */
    private void updateParents(
            SystemItem rootParent,
            long itemSize,
            Instant updateDate,
            Set<SystemItem> newParentsBranch
    ) {
        SystemItem current = rootParent;
        List<SystemItem> parents = new ArrayList<>();
        List<ItemUpdate> updates = new ArrayList<>();

        do {
            current.setDate(updateDate);
            current.setSize(current.getSize() - itemSize);
            parents.add(current);
            updates.add(new ItemUpdate(current));
            current = current.getParent();
            // Пока не встретим первого родителя, который будет обновлен в другом методе
        } while (current != null && !newParentsBranch.contains(current));

        systemItemRepo.saveAll(parents);
        itemUpdateRepo.saveAll(updates);
    }

    /**
     * Рекурсивное вычисление размера элемента.
     * Используется кэширование с помощью таблицы.
     * @param item элемент, размер которого требуется установить
     * @param knownSizes таблица с известными размерами, которая заполняется по мере выполнения метода
     * @return размер элемента
     */
    private Long getItemSize(SystemItem item, Map<String, Long> knownSizes) {
        Long size;

        if (item.getType() == SystemItemType.FILE) {
            size = item.getSize();
        } else {
            size = knownSizes.get(item.getId());

            if (size == null) {
                size = 0L;
                Set<SystemItem> children = item.getChildren();

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

    /**
     * Получение единственного элемента.
     * @param id идентификатор элемента
     * @return объект с информацией об элементе
     */
    @Transactional
    public ItemResponse getNode(String id) {
        SystemItem rootItem = findById(id);
        ItemResponse response = new ItemResponse(rootItem);
        setChildren(response, rootItem.getChildren());
        return response;
    }

    /**
     * Рекурсивное заполнение узла дочерними элементами.
     * @param response текущий элемент
     * @param itemChildren дети текущего элемента
     */
    private void setChildren(ItemResponse response, Set<SystemItem> itemChildren) {
        List<ItemResponse> responseChildren = null;

        if (response.getType() == SystemItemType.FOLDER) {
            responseChildren = new ArrayList<>();
            ItemResponse currentResponse;

            if (itemChildren != null) {
                for (SystemItem item : itemChildren) {
                    currentResponse = new ItemResponse(item);

                    if (item.getType() == SystemItemType.FILE) {
                        currentResponse.setChildren(null);
                    } else {
                        setChildren(currentResponse, item.getChildren());
                    }

                    responseChildren.add(currentResponse);
                }
            }
        }

        response.setChildren(responseChildren);
    }

    /**
     * Получение всех файлов, обновленных за последние 24 часа от времени запроса.
     * @param dateTo конец интервала
     * @return объект с найденными файлами
     */
    @Transactional
    public SystemItemHistoryResponse getLastUpdatedFiles(Instant dateTo) {
        Instant dateFrom = dateTo.minus(24, ChronoUnit.HOURS);
        List<SystemItem> items = systemItemRepo.findAllByDateBetweenAndType(dateFrom, dateTo, SystemItemType.FILE);
        List<SystemItemHistoryUnit> units = items.stream().map(SystemItemHistoryUnit::new).toList();

        return new SystemItemHistoryResponse(units);
    }

    /**
     * Получение истории обновлений элемента за заданный полуинтервал [dateStart, dateEnd).
     * @param id идентификатор элемента
     * @param dateStart начало интервала для поиска
     * @param dateEnd конец интервала для поиска
     * @return объект с найденными обновлениями
     */
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
