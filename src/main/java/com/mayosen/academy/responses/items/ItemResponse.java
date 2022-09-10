package com.mayosen.academy.responses.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.SystemItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "url", "type", "parentId", "date", "size", "children"})
public class ItemResponse {
    private String id;

    private String url;

    private SystemItemType type;

    @JsonIgnore
    private SystemItem parent;

    private Instant date;

    private Long size;

    private List<ItemResponse> children;

    @JsonProperty("parentId")
    public String getParentId() {
        return parent != null ? parent.getId() : null;
    }

    @Override
    public String toString() {
        return "ItemResponse{" +
                "id='" + id + '\'' +
                ", type=" + type +
                '}';
    }

    public ItemResponse(SystemItem item) {
        this.id = item.getId();
        this.url = item.getUrl();
        this.type = item.getType();
        this.parent = item.getParent();
        this.date = item.getDate();
    }
}
