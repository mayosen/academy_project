package com.mayosen.academy.repos;

import com.mayosen.academy.domain.ItemUpdate;
import com.mayosen.academy.domain.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ItemUpdateRepo extends CrudRepository<ItemUpdate, Long> {
    List<ItemUpdate> findAllByItem(Item item);

    @Query("SELECT u FROM ItemUpdate u WHERE u.item = :item AND u.date >= :dateStart")
    List<ItemUpdate> findAllByItemAndDateFrom(Item item, Instant dateStart);

    @Query("SELECT u FROM ItemUpdate u WHERE u.item = :item AND u.date < :dateEnd")
    List<ItemUpdate> findAllByItemAndDateTo(Item item, Instant dateEnd);

    @Query("SELECT u FROM ItemUpdate u WHERE u.item = :item AND :dateStart <= u.date AND u.date < :dateEnd")
    List<ItemUpdate> findAllByItemAndDateInterval(Item item, Instant dateStart, Instant dateEnd);
}
