package com.mayosen.academy.repos;

import com.mayosen.academy.domain.Item;
import com.mayosen.academy.domain.ItemType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ItemRepo extends CrudRepository<Item, String> {
    List<Item> findAllByDateBetweenAndType(Instant dateStart, Instant dateEnd, ItemType type);
}
