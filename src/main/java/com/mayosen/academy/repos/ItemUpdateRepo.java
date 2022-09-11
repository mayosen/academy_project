package com.mayosen.academy.repos;

import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.ItemUpdate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ItemUpdateRepo extends CrudRepository<ItemUpdate, Long> {
    List<ItemUpdate> findAllByItem(SystemItem item);

    @Query("SELECT u FROM ItemUpdate u WHERE u.item = :item AND u.date >= :dateStart")
    List<ItemUpdate> findAllByItemAndDateFrom(SystemItem item, Instant dateStart);

    @Query("SELECT u FROM ItemUpdate u WHERE u.item = :item AND u.date < :dateEnd")
    List<ItemUpdate> findAllByItemAndDateTo(SystemItem item, Instant dateEnd);

    @Query("SELECT u FROM ItemUpdate u WHERE u.item = :item AND :dateStart <= u.date AND u.date < :dateEnd")
    List<ItemUpdate> findAllByItemAndDateInterval(SystemItem item, Instant dateStart, Instant dateEnd);
}
