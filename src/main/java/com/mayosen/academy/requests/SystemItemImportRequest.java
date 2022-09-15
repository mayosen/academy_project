package com.mayosen.academy.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

/**
 * Запрос на добавление/изменение элементов.
 */
@Getter
@Setter
@NoArgsConstructor
public class SystemItemImportRequest {
    @NotNull
    @UniqueElements
    @Valid
    private List<SystemItemImport> items;

    @NotNull
    private Instant updateDate;
}
