package com.mayosen.academy.responses.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mayosen.academy.domain.Item;
import com.mayosen.academy.domain.ItemType;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * Объект с информацией об элементе. Включает в себя всех его детей.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "type"})
@JsonPropertyOrder({"id", "url", "type", "parentId", "date", "size", "children"})
public class ItemResponse {
    private String id;

    private String url;

    private ItemType type;

    @JsonIgnore
    private Item parent;

    @JsonProperty("parentId")
    public String getParentId() {
        return parent != null ? parent.getId() : null;
    }

    private Instant date;

    private Long size;

    private List<ItemResponse> children;

    public ItemResponse(Item item) {
        id = item.getId();
        url = item.getUrl();
        type = item.getType();
        parent = item.getParent();
        date = item.getDate();
        size = item.getSize();
    }
}
