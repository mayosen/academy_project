package com.mayosen.academy.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "update_history")
@Getter
@Setter
@NoArgsConstructor
public class ItemUpdate {
    @Id
    @Column(name = "update_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private SystemItem item;

    @Column(name = "url")
    private String url;

    @Column(name = "date")
    private Instant date;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private SystemItemType type;

    @Column(name = "size")
    private Long size;
}
