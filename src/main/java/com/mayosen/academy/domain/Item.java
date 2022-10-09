package com.mayosen.academy.domain;

import com.mayosen.academy.requests.ItemImportRequest;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Сущность элемента.
 */
@Entity
@Table(name = "system_item")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "type", "size"})
public class Item implements Serializable {
    @Id
    @Column(name = "item_id")
    private String id;

    @Column(name = "url")
    private String url;

    @Column(name = "date")
    private Instant date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Item parent;

    /**
     * Служебное поле для связи элемента и предполагаемого нового родителя.
     * @see com.mayosen.academy.services.ItemService#updateItems(ItemImportRequest)
     */
    @Transient
    private String newParentId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Column(name = "size")
    private Long size;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Item> children = new LinkedHashSet<>();
}
