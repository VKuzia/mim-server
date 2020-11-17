package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.ChatMessageEntity;
import org.springframework.data.repository.CrudRepository;

public interface ChatMessageRepository extends CrudRepository<ChatMessageEntity, Integer> {
}
