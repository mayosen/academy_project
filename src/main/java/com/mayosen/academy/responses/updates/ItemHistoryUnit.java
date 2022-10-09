package com.mayosen.academy.responses.updates;

import com.mayosen.academy.domain.ItemUpdate;
import com.mayosen.academy.domain.Item;
import com.mayosen.academy.domain.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Объект, описывающий единственный элемент.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemHistoryUnit {
    private String id;
    private String url;
    private String parentId;
    private ItemType type;
    private Long size;
    private Instant date;

    public ItemHistoryUnit(ItemUpdate update) {
        id = update.getItem().getId();
        url = update.getUrl();
        parentId = update.getParentId();
        type = update.getType();
        size = update.getSize();
        date = update.getDate();
    }

    public ItemHistoryUnit(Item item) {
        id = item.getId();
        url = item.getUrl();
        parentId = item.getParent() != null ? item.getParent().getId() : null;
        type = item.getType();
        size = item.getSize();
        date = item.getDate();
    }
}
