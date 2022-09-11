package com.mayosen.academy.responses.updates;

import com.mayosen.academy.domain.ItemUpdate;
import com.mayosen.academy.domain.SystemItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemItemHistoryUnit {
    private String id;
    private String url;
    private String parentId;
    private SystemItemType type;
    private Long size;
    private Instant date;

    public SystemItemHistoryUnit(ItemUpdate update) {
        id = update.getItem().getId();
        url = update.getUrl();
        parentId = update.getParentId();
        type = update.getType();
        size = update.getSize();
        date = update.getDate();
    }
}
