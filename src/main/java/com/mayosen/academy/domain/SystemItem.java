package com.mayosen.academy.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "system_item")
@Getter
@Setter
@NoArgsConstructor
public class SystemItem implements Serializable {
    @Id
    @Column(name = "item_id")
    private String id;

    @Column(name = "url")
    private String url;

    @Column(name = "date", nullable = false)
    private Instant date;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SystemItemType type;

    @Column(name = "size")
    private Long size;
}
