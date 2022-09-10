package com.mayosen.academy.repos;

import com.mayosen.academy.domain.SystemItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemItemRepo extends CrudRepository<SystemItem, String> {
    @Query("SELECT item from SystemItem item WHERE item.parentId = :itemId")
    List<SystemItem> findChildrenByItemId(@Param("itemId") String itemId);
}
