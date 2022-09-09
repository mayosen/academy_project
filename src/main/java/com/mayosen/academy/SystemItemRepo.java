package com.mayosen.academy;

import com.mayosen.academy.domain.SystemItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemItemRepo extends CrudRepository<SystemItem, String> {
}
