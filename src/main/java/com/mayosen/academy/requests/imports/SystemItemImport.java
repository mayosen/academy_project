package com.mayosen.academy.requests.imports;

import com.mayosen.academy.domain.SystemItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class SystemItemImport {
    @NotEmpty
    private String id;

    private String url;

    private String parentId;

    @NotNull
    private SystemItemType type;

    private Long size;
}
