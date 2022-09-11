package com.mayosen.academy.repos;

import com.mayosen.academy.domain.SystemItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SystemItemRepo extends CrudRepository<SystemItem, String> {
    List<SystemItem> findAllByDateBetween(Instant from, Instant to);
}
