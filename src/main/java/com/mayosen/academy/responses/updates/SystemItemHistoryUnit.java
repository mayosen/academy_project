package com.mayosen.academy.responses.updates;

import com.mayosen.academy.domain.SystemItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class SystemItemHistoryUnit {
    private String id;
    private String url;
    private String parentId;
    private SystemItemType type;
    private long size;
    private Instant date;
}
