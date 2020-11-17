package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import org.springframework.data.repository.CrudRepository;

public interface ChatMessagesRepository extends CrudRepository<ChatMessageEntity, Integer> {
}
