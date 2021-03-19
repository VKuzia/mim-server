package com.mimteam.mimserver.repositories;

import com.mimteam.mimserver.model.entities.chat.ChatMessageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChatMessagesRepository extends CrudRepository<ChatMessageEntity, Integer> {
    List<ChatMessageEntity> findByChatId(Integer chatId);
}
