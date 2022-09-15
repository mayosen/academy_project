package com.mayosen.academy.repos;

import com.mayosen.academy.domain.SystemItem;
import com.mayosen.academy.domain.SystemItemType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SystemItemRepo extends CrudRepository<SystemItem, String> {
    List<SystemItem> findAllByDateBetweenAndType(Instant dateStart, Instant dateEnd, SystemItemType type);
}
