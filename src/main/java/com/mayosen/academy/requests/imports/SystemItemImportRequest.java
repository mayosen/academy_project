package com.mayosen.academy.requests.imports;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SystemItemImportRequest {
    @NotEmpty
    private List<@Valid SystemItemImport> items;

    @NotNull
    private Instant updateDate;
}
