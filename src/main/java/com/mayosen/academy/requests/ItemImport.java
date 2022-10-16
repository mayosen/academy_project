package com.mayosen.academy.requests;

import com.mayosen.academy.domain.ItemType;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * Запрос на добавление/изменение объекта.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "type"})
public class ItemImport {
    @NotNull
    @Length(max = 255)
    private String id;

    @Length(max = 255)
    private String url;

    @Length(max = 255)
    private String parentId;

    @NotNull
    private ItemType type;

    private Long size;
}
