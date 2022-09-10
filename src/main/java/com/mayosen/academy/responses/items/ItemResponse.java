package com.mayosen.academy.responses.items;

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
public class ItemResponse {
    private String id;

    private String url;

    private SystemItemType type;

    private String parentId;

    private Instant date;

    private Long size;

    private List<ItemResponse> children;
}
