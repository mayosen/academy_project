package com.mayosen.academy.requests.imports;

import com.mayosen.academy.domain.SystemItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class SystemItemImport {
    @NotEmpty
    @Length(max = 255)
    private String id;

    @Length(max = 255)
    private String url;

    @Length(max = 255)
    private String parentId;

    @NotNull
    private SystemItemType type;

    private Long size;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemItemImport that = (SystemItemImport) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
