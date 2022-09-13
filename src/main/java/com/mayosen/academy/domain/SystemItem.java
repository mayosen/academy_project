package com.mayosen.academy.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "system_item")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "type", "size"})
public class SystemItem implements Serializable {
    @Id
    @Column(name = "item_id")
    private String id;

    @Column(name = "url")
    private String url;

    @Column(name = "date")
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private SystemItem parent;

    @Transient
    private String parentId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private SystemItemType type;

    @Column(name = "size")
    private Long size;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<SystemItem> children = new LinkedHashSet<>();
}
