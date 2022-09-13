package com.mayosen.academy.responses.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.SystemItemType;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "type"})
@JsonPropertyOrder({"id", "url", "type", "parentId", "date", "size", "children"})
public class ItemResponse {
    private String id;

    private String url;

    private SystemItemType type;

    @JsonIgnore
    private SystemItem parent;

    @JsonProperty("parentId")
    public String getParentId() {
        return parent != null ? parent.getId() : null;
    }

    private Instant date;

    private Long size;

    private List<ItemResponse> children;

    public ItemResponse(SystemItem item) {
        id = item.getId();
        url = item.getUrl();
        type = item.getType();
        parent = item.getParent();
        date = item.getDate();
        size = item.getSize();
    }
}
