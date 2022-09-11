package com.mayosen.academy.requests.imports;

import com.mayosen.academy.domain.SystemItemType;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "type"})
public class SystemItemImport {
    @NotNull
    @Length(max = 255)
    private String id;

    @Length(max = 255)
    private String url;

    @Length(max = 255)
    private String parentId;

    @NotNull
    private SystemItemType type;

    private Long size;
}
