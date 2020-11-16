package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.СhatMessageEntity;
import org.springframework.data.repository.CrudRepository;

public interface ChatMessageRepository extends CrudRepository<СhatMessageEntity, Integer> {
}
