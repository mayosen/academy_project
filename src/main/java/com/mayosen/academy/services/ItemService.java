package com.mayosen.academy.services;

import com.mayosen.academy.domain.ItemUpdate;
import com.mayosen.academy.domain.Item;
import com.mayosen.academy.domain.ItemType;
import com.mayosen.academy.exceptions.ItemNotFoundException;
import com.mayosen.academy.repos.ItemUpdateRepo;
import com.mayosen.academy.repos.ItemRepo;
import com.mayosen.academy.requests.ItemImport;
import com.mayosen.academy.requests.ItemImportRequest;
import com.mayosen.academy.responses.items.ItemResponse;
import com.mayosen.academy.responses.updates.ItemHistoryResponse;
import com.mayosen.academy.responses.updates.ItemHistoryUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ItemService {
    private final ItemRepo itemRepo;
    private final ItemUpdateRepo itemUpdateRepo;

    @Autowired
    public ItemService(ItemRepo itemRepo, ItemUpdateRepo itemUpdateRepo) {
        this.itemRepo = itemRepo;
        this.itemUpdateRepo = itemUpdateRepo;
    }

    /**
     * Добавление и обновление элементов.
     * @param request объект с обновляемыми элементами
     * @throws ValidationException ошибка валидации данных
     */
    @Transactional
    public void updateItems(ItemImportRequest request) {
        Instant updateDate = request.getUpdateDate();
        int itemsSize = request.getItems().size();
        // Таблица для быстрого поиска новых родителей из запроса
        Map<String, Item> mappedItems = new HashMap<>(itemsSize);

        for (ItemImport importItem : request.getItems()) {
            Item item = itemRepo.findById(importItem.getId()).orElseGet(Item::new);

            if (item.getId() != null && importItem.getType() != item.getType()) {
                throw new ValidationException("Нельзя менять тип элемента");
            }

            if (importItem.getType() == ItemType.FOLDER) {
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

            if (importItem.getType() == ItemType.FILE) {
                item.setSize(importItem.getSize());
            }

            mappedItems.put(item.getId(), item);
        }

        List<ItemParentPair> oldParents = new ArrayList<>();

        for (Item item : mappedItems.values()) {
            String newParentId = item.getNewParentId();

            // Если есть старый родитель, id которого отличается от обновляемого (включая null)
            if (item.getParent() != null && !item.getParent().getId().equals(newParentId)) {
                oldParents.add(new ItemParentPair(item));
            }

            if (newParentId != null) {
                Item parent = mappedItems.get(newParentId);

                if (parent == null) {
                    parent = itemRepo.findById(newParentId)
                            .orElseThrow(() -> new ValidationException("Родитель не найден"));
                }

                if (parent.getType() != ItemType.FOLDER) {
                    throw new ValidationException("Родителем может быть только папка");
                }

                item.setParent(parent);
                parent.getChildren().add(item);
            }
        }

        for (ItemParentPair pair : oldParents) {
            Item oldParent = pair.oldParent;
            Item item = pair.item;
            oldParent.getChildren().remove(item);
            Item current = item;
            Set<Item> newParentsBranch = new HashSet<>();

            while (current.getParent() != null) {
                current = current.getParent();
                newParentsBranch.add(current);
            }

            updateParents(oldParent, item.getSize(), updateDate, newParentsBranch);
        }

        Map<String, Long> knownSizes = new HashMap<>(itemsSize);
        Set<Item> sortedItems = new LinkedHashSet<>(itemsSize);
        List<ItemUpdate> updates = new ArrayList<>(itemsSize);

        // Сортировка нужна, чтобы сохранить родителей вперед детей
        for (Item item : mappedItems.values()) {
            Long size;
            Item current = item;
            Deque<Item> childBranch = new LinkedList<>();

            // Устанавливаем размер новым родителям
            do {
                current.setDate(updateDate);
                size = getItemSize(current, knownSizes);
                current.setSize(size);
                childBranch.addFirst(current);
                current = current.getParent();
            } while (current != null);

            sortedItems.addAll(childBranch);
        }

        for (Item item : sortedItems) {
            // Дети уже присутствуют среди элементов на сохранение
            item.getChildren().clear();
            updates.add(new ItemUpdate(item));
        }

        itemRepo.saveAll(sortedItems);
        itemUpdateRepo.saveAll(updates);
    }

    /**
     * Класс для связи элемента и его старого родителя.
     */
    private static class ItemParentPair {
        Item oldParent;
        Item item;

        public ItemParentPair(Item item) {
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
        Item item = findById(id);
        itemRepo.delete(item);

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
    private Item findById(String id) {
        return itemRepo.findById(id).orElseThrow(ItemNotFoundException::new);
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
            Item rootParent,
            long itemSize,
            Instant updateDate,
            Set<Item> newParentsBranch
    ) {
        Item current = rootParent;
        List<Item> parents = new ArrayList<>();
        List<ItemUpdate> updates = new ArrayList<>();

        do {
            current.setDate(updateDate);
            current.setSize(current.getSize() - itemSize);
            parents.add(current);
            updates.add(new ItemUpdate(current));
            current = current.getParent();
            // Пока не встретим первого родителя, который будет обновлен в другом методе
        } while (current != null && !newParentsBranch.contains(current));

        itemRepo.saveAll(parents);
        itemUpdateRepo.saveAll(updates);
    }

    /**
     * Рекурсивное вычисление размера элемента.
     * Используется кэширование с помощью таблицы.
     * @param item элемент, размер которого требуется установить
     * @param knownSizes таблица с известными размерами, которая заполняется по мере выполнения метода
     * @return размер элемента
     */
    private Long getItemSize(Item item, Map<String, Long> knownSizes) {
        Long size;

        if (item.getType() == ItemType.FILE) {
            size = item.getSize();
        } else {
            size = knownSizes.get(item.getId());

            if (size == null) {
                size = 0L;
                Set<Item> children = item.getChildren();

                for (Item child : children) {
                    Long currentSize = getItemSize(child, knownSizes);
                    size += currentSize;
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
        Item rootItem = findById(id);
        ItemResponse response = new ItemResponse(rootItem);
        setChildren(response, rootItem.getChildren());
        return response;
    }

    /**
     * Рекурсивное заполнение узла дочерними элементами.
     * @param response текущий элемент
     * @param itemChildren дети текущего элемента
     */
    private void setChildren(ItemResponse response, Set<Item> itemChildren) {
        List<ItemResponse> responseChildren = null;

        if (response.getType() == ItemType.FOLDER) {
            responseChildren = new ArrayList<>();
            ItemResponse currentResponse;

            if (itemChildren != null) {
                for (Item item : itemChildren) {
                    currentResponse = new ItemResponse(item);

                    if (item.getType() == ItemType.FILE) {
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
    public ItemHistoryResponse getLastUpdatedFiles(Instant dateTo) {
        Instant dateFrom = dateTo.minus(24, ChronoUnit.HOURS);
        List<Item> items = itemRepo.findAllByDateBetweenAndType(dateFrom, dateTo, ItemType.FILE);
        List<ItemHistoryUnit> units = items.stream().map(ItemHistoryUnit::new).toList();

        return new ItemHistoryResponse(units);
    }

    /**
     * Получение истории обновлений элемента за заданный полуинтервал [dateStart, dateEnd).
     * @param id идентификатор элемента
     * @param dateStart начало интервала для поиска
     * @param dateEnd конец интервала для поиска
     * @return объект с найденными обновлениями
     */
    @Transactional
    public ItemHistoryResponse getNodeHistory(String id, Instant dateStart, Instant dateEnd) {
        Item item = findById(id);
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

        List<ItemHistoryUnit> units = updates.stream().map(ItemHistoryUnit::new).toList();

        return new ItemHistoryResponse(units);
    }
}
